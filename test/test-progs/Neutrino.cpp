
#include <chrono>
#include <iostream>
#include <thread>

#include "gtest/gtest.h"

TEST(Neutrino, HasMass) {
  const double massInEv = 0.10;
  EXPECT_LT(0, massInEv);
  EXPECT_GT(0.28, massInEv);
}

TEST(Neutrino, IsStable) {
  const size_t pid = std::hash<std::thread::id>()(std::this_thread::get_id());
  const bool isStable = pid % 2;
  std::cerr << "Hello from cerr" << std::endl;
  EXPECT_TRUE(isStable);
}

TEST(Neutrino, MeasureMeanFreePath) {
    std::cout << "Start measuring mean free path" << std::endl;
    for (int i = 0; i < 10; ++i) {
        std::this_thread::sleep_for(std::chrono::milliseconds(300));
        std::cout << "Still doing..." << std::endl;
    }
    EXPECT_TRUE("The path is very very long...");
}

TEST(UrcaProcess, InvolvesNeutrino) {
  const std::string reaction("e+ + n -> p + ~nu");
  EXPECT_FALSE(reaction.empty());
}

