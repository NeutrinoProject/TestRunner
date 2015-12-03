
#include "gtest/gtest.h"

struct Neutralino {
  ~Neutralino() {
    exit(42);
  }
};

TEST(Neutralino, Exists) {
  static const Neutralino neutralino;
}

