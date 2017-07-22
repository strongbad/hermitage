package com.github.strongbad.hermitage;

import com.github.strongbad.hermitage.transaction.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.TransactionException;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.github.strongbad.hermitage.transaction.SqlStatement.sql;
import static com.github.strongbad.hermitage.transaction.StepResult.UnwrappedValue.unwrappedValueHolder;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class G2ItemBehaviour {

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
  public void G2Item_2PC() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult> updateOnT2 = unwrappedValueHolder();

    sql("select * from test where id in (1, 2)").select(T1).waitForCompletion();
    sql("select * from test where id in (1, 2)").select(T2).waitForCompletion();
    sql("update test set value = 11 where id = 1").update(T1);
    sql("update test set value = 21 where id = 2").update(T2).into(updateOnT2);

    Optional<RuntimeException> exception = updateOnT2.get().exception();

    assertTrue(exception.isPresent());
    assertEquals(DeadlockLoserDataAccessException.class, exception.get().getClass());

    T1.commit();
    T2.rollback();
  }

  @Test
  public void G2Item_SSI() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult> updateOnT2 = unwrappedValueHolder();

    sql("select * from test where id in (1, 2)").select(T1).waitForCompletion();
    sql("select * from test where id in (1, 2)").select(T2).waitForCompletion();
    sql("update test set value = 11 where id = 1").update(T1).waitForCompletion();
    sql("update test set value = 21 where id = 2").update(T2).waitForCompletion();

    T1.commit();
    T2.commit().into(updateOnT2);

    Optional<RuntimeException> exception = updateOnT2.get().exception();
    assertEquals(TransactionException.class, exception.get().getClass());
  }

}
