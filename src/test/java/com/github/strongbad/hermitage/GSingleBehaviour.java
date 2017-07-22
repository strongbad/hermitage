package com.github.strongbad.hermitage;

import com.github.strongbad.hermitage.transaction.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.github.strongbad.hermitage.model.TestRow.row;
import static com.github.strongbad.hermitage.transaction.SqlStatement.sql;
import static com.github.strongbad.hermitage.transaction.StepResult.UnwrappedValue.unwrappedValueHolder;
import static com.github.strongbad.hermitage.transaction.StepResult.WrappedValue.wrappedValueHolder;
import static java.util.Collections.singletonList;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GSingleBehaviour {

  @Autowired
  private TransactionFactory transactionFactory;

  @Before
  public void setup() {
    Transaction transaction = transactionFactory.startTransaction();
    sql("delete from test").update(transaction).waitForCompletion();
    sql("insert into test (id, value) values (1, 10), (2, 20)").insert(transaction).waitForCompletion();
    transaction.commit().waitForCompletion();
  }

  @After
  public void cleanup() {
    transactionFactory.closeTransactions();
  }

  @Test
  public void GSingle() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult.ResultSet> selectOnT1 = unwrappedValueHolder();
    StepResult.WrappedValue<SqlResult> updateOnT2 = wrappedValueHolder();
    StepResult.UnwrappedValue<SqlResult> deleteOnT1 = unwrappedValueHolder();

    sql("select * from test where id = 1").select(T1).into(selectOnT1);
    assertEquals(singletonList(row(1, 10)), selectOnT1.get().getResults());

    sql("select * from test").select(T2).waitForCompletion();

    sql("update test set value = 12 where id = 1").update(T2).into(updateOnT2);
    assertTrue(updateOnT2.get().isBlocking());

    sql("delete from test where value = 20").delete(T1).into(deleteOnT1);

    Optional<RuntimeException> exception = deleteOnT1.get().exception();

    assertTrue(exception.isPresent());
    assertEquals(DeadlockLoserDataAccessException.class, exception.get().getClass());

    sql("update test set value = 18 where id = 2").update(T2);

    T1.rollback();
    T2.commit();
  }

}
