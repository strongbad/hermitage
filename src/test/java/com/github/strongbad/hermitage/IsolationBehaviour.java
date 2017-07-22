package com.github.strongbad.hermitage;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
  G0Behaviour.class,
  G1aBehaviour.class,
  G1bBehaviour.class,
  G1cBehaviour.class,
  OTVBehaviour.class,
  PMPBehaviour.class,
  P4Behaviour.class,
  G2ItemBehaviour.class,
  G2Behaviour.class
})
public class IsolationBehaviour {}
