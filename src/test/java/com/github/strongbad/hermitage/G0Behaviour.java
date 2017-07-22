package com.github.strongbad.hermitage;

import com.github.strongbad.hermitage.transaction.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static com.github.strongbad.hermitage.model.TestRow.row;
import static com.github.strongbad.hermitage.transaction.SqlStatement.sql;
import static com.github.strongbad.hermitage.transaction.StepResult.UnwrappedValue.unwrappedValueHolder;
import static com.github.strongbad.hermitage.transaction.StepResult.WrappedValue.wrappedValueHolder;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class G0Behaviour {

  @Autowired
  private TransactionFactory transactionFactory;

  @Before
  public void setup() {
    Transaction transaction = transactionFactory.startTransaction();
    sql("delete from test").update(transaction).waitForCompletion();
    sql("insert into test (id, value) values (1, 10), (2, 20)").insert(transaction).waitForCompletion();
    transaction.commit();
  }

  @After
  public void cleanup() {
    transactionFactory.closeTransactions();
  }

  @Test
  public void G0() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult.ResultSet> selectOnT1 = unwrappedValueHolder();
    StepResult.WrappedValue<SqlResult> updateOnT2 = wrappedValueHolder();

    sql("update test set value = 11 where id = 1").update(T1).waitForCompletion();
    sql("update test set value = 12 where id = 1").update(T2).into(updateOnT2);
    assertTrue(updateOnT2.get().isBlocking());

    sql("update test set value = 21 where id = 2").update(T1).waitForCompletion();

    T1.commit().waitForCompletion();

    assertFalse(updateOnT2.get().isBlocking());

    T1 = transactionFactory.startTransaction();

    sql("select * from test").select(T1).into(selectOnT1);
    assertEquals(asList(row(1, 11), row(2, 21)), selectOnT1.get().getResults());

    sql("update test set value = 22 where id = 2").update(T2);
    T2.commit().waitForCompletion();

    sql("select * from test").select(T1).into(selectOnT1);
    assertEquals(asList(row(1, 12), row(2, 22)), selectOnT1.get().getResults());
  }

}
