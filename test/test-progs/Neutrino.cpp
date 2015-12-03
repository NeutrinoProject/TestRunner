
#include <chrono>
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
  EXPECT_TRUE(isStable);
}

TEST(Neutrino, MeasureMeanFreePath) {
    std::this_thread::sleep_for(std::chrono::seconds(3));
    EXPECT_TRUE("The path is very very long...");
}

TEST(UrcaProcess, InvolvesNeutrino) {
  const std::string reaction("e+ + n -> p + ~nu");
  EXPECT_FALSE(reaction.empty());
}
