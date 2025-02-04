# ProducerConsumer

I implemented 2 versions of the Queue, which are thread safe, lock free, and can be used for multiple producer threads and multiple consumer threads.

1. [AtomicIntegerRingBuffer](src%2Fmain%2Fjava%2Fcom%2Fallison%2Fservice%2FAtomicIntegerRingBuffer.java)
-- Similar to what we discussed in the call, using AtomicInteger for CAS operations.  

2. [UnsafeRingBuffer](src%2Fmain%2Fjava%2Fcom%2Fallison%2Fservice%2FUnsafeRingBuffer.java)
-- it is referenced the RingBuffer implementation in Disruptor, it uses Unsafe to operate off-heap buffer, and it uses Padding to separate producer / consumer in different cache lines to avoid false sharing


To achieve better performance, I improved the code with below:
1. All memory allocation is done during the startup phase, without allocating new objects during the operation phase
2. Presize the collections without resizing during the operation phase
3. Implement my own logger SystemOutLogger to avoid creating many Strings in the heap
4. busy-spin the threads to monopolize CPU to avoid context switching