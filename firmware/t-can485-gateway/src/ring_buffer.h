#pragma once
#include <Arduino.h>

template <typename T, size_t Capacity>
class RingBuffer {
public:
  void push(const T& value) {
    items_[head_] = value;
    head_ = (head_ + 1) % Capacity;
    if (count_ < Capacity) count_++;
  }

  size_t size() const { return count_; }
  void clear() { head_ = 0; count_ = 0; }

  T atLatest(size_t indexFromNewest) const {
    if (indexFromNewest >= count_) return T();
    size_t index = (head_ + Capacity - 1 - indexFromNewest) % Capacity;
    return items_[index];
  }

private:
  T items_[Capacity]{};
  size_t head_ = 0;
  size_t count_ = 0;
};
