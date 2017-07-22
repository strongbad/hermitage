package com.github.strongbad.hermitage;

import com.github.strongbad.hermitage.transaction.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.ExecutionException;

import static com.github.strongbad.hermitage.transaction.SqlStatement.sql;
import static com.github.strongbad.hermitage.transaction.StepResult.UnwrappedValue.unwrappedValueHolder;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

@SpringBootTest
@RunWith(SpringRunner.class)
public class PMPBehaviour {

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
  public void PMP() throws InterruptedException, ExecutionException {
    Transaction T1 = transactionFactory.startTransaction();
    Transaction T2 = transactionFactory.startTransaction();

    StepResult.UnwrappedValue<SqlResult.ResultSet> selectOnT1 = unwrappedValueHolder();

    sql("select * from test where value = 30").select(T1).into(selectOnT1);
    assertEquals(emptyList(), selectOnT1.get().getResults());

    sql("insert into test (id, value) values(3, 30)").insert(T2);

    T2.commit().waitForCompletion();

    sql("select * from test where value % 3 = 0").select(T1).into(selectOnT1);
    assertEquals(emptyList(), selectOnT1.get().getResults());

    T1.commit().waitForCompletion();
  }

}
