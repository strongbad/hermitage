package com.github.strongbad.hermitage.transaction;

interface TransactionRunner extends Runnable {

  TransactionChannel getChannel();

}
