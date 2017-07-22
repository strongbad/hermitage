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
public class OTVBehaviour {

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
  public void OTV() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();
    Transaction T3 = transactionFactory.startTransaction();

    StepResult.WrappedValue<SqlResult> updateOnT2 = wrappedValueHolder();
    StepResult.UnwrappedValue<SqlResult.ResultSet> selectOnT3 = unwrappedValueHolder();

    sql("update test set value = 11 where id = 1").update(T1).waitForCompletion();
    sql("update test set value = 19 where id = 2").update(T1).waitForCompletion();
    sql("update test set value = 12 where id = 1").update(T2).into(updateOnT2);

    assertTrue(updateOnT2.get().isBlocking());

    T1.commit().waitForCompletion();

    assertFalse(updateOnT2.get().isBlocking());

    sql("select * from test").select(T3).into(selectOnT3);
    assertEquals(asList(row(1, 11), row(2, 19)), selectOnT3.get().getResults());

    sql("update test set value = 18 where id = 2").update(T2).waitForCompletion();

    sql("select * from test").select(T3).into(selectOnT3);
    assertEquals(asList(row(1, 11), row(2, 19)), selectOnT3.get().getResults());

    T2.commit().waitForCompletion();

    sql("select * from test").select(T3).into(selectOnT3);
    assertEquals(asList(row(1, 12), row(2, 18)), selectOnT3.get().getResults());

    T3.commit().waitForCompletion();
  }

}
