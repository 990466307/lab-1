package test;

import benchmark.internal.Benchmark;
import benchmark.objects.B;

public class Mytest {

  public static void main(String[] args) {
    Benchmark.alloc(3);
    B b = new B();
    Benchmark.alloc(4);
    B a = new B();
    Benchmark.test(7, b);
  }
}
/*
Answer:
  7 : 3
*/
