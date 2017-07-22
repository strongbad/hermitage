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

import static com.github.strongbad.hermitage.transaction.SqlStatement.sql;
import static com.github.strongbad.hermitage.transaction.StepResult.UnwrappedValue.unwrappedValueHolder;
import static com.github.strongbad.hermitage.transaction.StepResult.WrappedValue.wrappedValueHolder;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class G2Behaviour {

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
  public void G2() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.WrappedValue<SqlResult> insertOnT1 = wrappedValueHolder();
    StepResult.UnwrappedValue<SqlResult> insertOnT2 = unwrappedValueHolder();

    sql("select * from test where value % 3 = 0").select(T1).waitForCompletion();
    sql("select * from test where value % 3 = 0").select(T2).waitForCompletion();

    sql("insert into test (id, value) values(3, 30)").update(T1).into(insertOnT1);
    assertTrue(insertOnT1.get().isBlocking());

    sql("insert into test (id, value) values(4, 42)").update(T2).into(insertOnT2);

    Optional<RuntimeException> exception = insertOnT2.get().exception();

    assertTrue(exception.isPresent());
    assertEquals(DeadlockLoserDataAccessException.class, exception.get().getClass());

    T1.commit();
    T2.rollback();
  }

}
