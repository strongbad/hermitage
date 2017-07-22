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
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class G1aBehaviour {

  @Autowired
  private TransactionFactory transactionFactory;

  @Before
  public void setup() {
    Transaction transaction = transactionFactory.startTransaction();
    sql("delete from test").update(transaction).waitForCompletion();
    sql("insert into test (id, value) values (1, 10)").insert(transaction).waitForCompletion();
    transaction.commit().waitForCompletion();
  }

  @After
  public void cleanup() {
    transactionFactory.closeTransactions();
  }

  @Test
  public void G1a() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult.ResultSet> selectOnT2 = unwrappedValueHolder();

    sql("update test set value = 101 where id = 1").update(T1).waitForCompletion();
    sql("select * from test").select(T2).into(selectOnT2);

    assertEquals(singletonList(row(1, 10)), selectOnT2.get().getResults());

    T1.rollback().waitForCompletion();

    sql("select * from test").select(T2).into(selectOnT2);
    assertEquals(singletonList(row(1, 10)), selectOnT2.get().getResults());

    T2.commit();
  }

}
