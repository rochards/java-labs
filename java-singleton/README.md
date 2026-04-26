# Java Singleton

A singleton is a design pattern that ensures a class has only one instance during the application lifecycle and provides a global access point to that instance.

This project was created with assistance from OpenAI Codex.

## Purpose

This project presents different ways to implement a cache manager in Java, with and without the Singleton pattern.
It is intended as a learning project to compare the behavior of each approach, especially in concurrent scenarios.

The code shows:
- A regular non-singleton cache manager.
- A lazy singleton that is not thread-safe.
- A lazy singleton protected with `synchronized` and `volatile`.
- A singleton example using `AtomicReference`.
- An eager singleton created when the class is loaded.

The tests are used to demonstrate both the expected singleton behavior and the risks of unsafe lazy initialization.

## Tech Stack

- Java 21
- Maven
- JUnit 6

## Project Structure

### Main classes

- `src/main/java/com/github/rochards/java_singleton/nosingleton/CacheManager.java`: Generic cache manager without the Singleton pattern.
- `src/main/java/com/github/rochards/java_singleton/singleton/CacheManagerV1.java`: Lazy initialization singleton without thread-safety guarantees.
- `src/main/java/com/github/rochards/java_singleton/singleton/CacheManagerV2.java`: Lazy initialization singleton using double-checked locking with `volatile`.
- `src/main/java/com/github/rochards/java_singleton/singleton/CacheManagerV3.java`: Lazy initialization example using `AtomicReference` together with synchronization.
- `src/main/java/com/github/rochards/java_singleton/singleton/CacheManagerV4.java`: Eager initialization singleton.

### Test classes

- `src/test/java/com/github/rochards/java_singleton/nosingleton/CacheManagerTest.java`
- `src/test/java/com/github/rochards/java_singleton/singleton/CacheManagerV1Test.java`
- `src/test/java/com/github/rochards/java_singleton/singleton/CacheManagerV2Test.java`
- `src/test/java/com/github/rochards/java_singleton/singleton/CacheManagerV3Test.java`
- `src/test/java/com/github/rochards/java_singleton/singleton/CacheManagerV4Test.java`

Special attention:

- `CacheManagerV1Test.shouldExposeThatLazyInitializationIsNotThreadSafe()` is the test that intentionally explores the race condition in the unsafe lazy singleton.
- `CacheManagerV2Test.shouldVerifyThereIsOneInstanceCreated()` repeats the same concurrent access scenario for the synchronized lazy singleton and verifies that only one instance is created.
- `CacheManagerV3Test.shouldVerifyThereIsOneInstanceCreated()` and `CacheManagerV4Test.shouldVerifyThereIsOneInstanceCreated()` confirm that the `AtomicReference` and eager initialization approaches also keep a single instance under concurrent access.

## Singleton Variations

### No singleton

`CacheManager` is a regular class. Because its constructor is public, any code can create as many instances as needed.

### Lazy initialization

`CacheManagerV1` uses lazy initialization because the instance is only created when `getInstance()` is called for the first time.
This version is intentionally unsafe in multithreaded scenarios, and the test demonstrates how multiple instances can be created when several threads enter the initialization path at the same time.

### Lazy initialization with synchronization

`CacheManagerV2` also uses lazy initialization, but protects instance creation with `synchronized`.
It uses double-checked locking, which is why the `instance` field must be declared as `volatile`.
This version avoids creating multiple instances when several threads call `getInstance()` concurrently.

### AtomicReference example

`CacheManagerV3` uses `AtomicReference<CacheManagerV3>` to hold the shared instance.
The code first reads the reference outside the lock to avoid synchronization after initialization, then reads it again inside the synchronized block before creating the object.

This is useful as an example because it introduces atomic references and safe publication of shared state.
It is also a good place to compare different concurrency tools:

- `volatile` for visibility guarantees
- `synchronized` for mutual exclusion
- `AtomicReference` for safe shared reference updates

Another possible approach with `AtomicReference` is using `compareAndSet(null, newInstance)`.
This avoids explicit synchronization and lets multiple threads race to install the singleton safely, with only one thread winning the update.

The tradeoff is that more than one thread may still execute `new CacheManagerV3()` before one of them wins the `compareAndSet`.
Only one instance is stored and used as the singleton, but extra temporary objects may still be created and discarded.
Because of that, `compareAndSet` is usually a better fit when object construction is cheap and side-effect free.

### Eager initialization

`CacheManagerV4` uses eager initialization.
The singleton instance is created when the class is loaded:

```java
private static final CacheManagerV4 INSTANCE = new CacheManagerV4();
```

This is simple and thread-safe because Java class loading is thread-safe.
The tradeoff is that the instance is created even if the application never uses it.


### Comparison Table

| Implementation | Initialization style | Thread-safe | Main advantage | Main tradeoff |
| --- | --- | --- | --- | --- |
| `CacheManager` | Not a singleton | No | Simple | Does not enforce a single shared instance |
| `CacheManagerV1` | Lazy | No | Very simple lazy initialization | Can create multiple instances in concurrent access |
| `CacheManagerV2` | Lazy | Yes | Thread-safe lazy initialization with low locking after creation | More complex due to `volatile` and double-checked locking |
| `CacheManagerV3` | Lazy | Yes | Good example of safe shared reference handling with `AtomicReference` | More complex than necessary for a basic singleton |
| `CacheManagerV4` | Eager | Yes | Simple and naturally thread-safe | Creates the instance even if it is never used |


## Notes

- `CacheManagerV1`, `CacheManagerV2` and `CacheManagerV2` contain an artificial `Thread.sleep(...)` to widen the race window for test and demonstration purposes.
- The tests use `CountDownLatch` and an executor to coordinate concurrent calls to `getInstance()`.

## Running the tests

```bash
mvn test
```

## Possible next steps

- Add the initialization-on-demand holder idiom as another singleton variant.
- Add an enum-based singleton example.
- Add benchmarks or simple timing comparisons between eager and lazy approaches.
