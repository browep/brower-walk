The Java project is built using gradle. The gradle wrapper should download gradle and create a runnable jar.  You will need to have Java 1.8 installed.

From the repo root on a *nix system.
````
cd java
./gradlew clean build
````

This will create a jar in `build/libs/`

Run the jar with 
```
java -jar build/libs/brower-walk-*.jar
```

There is an optional `-t NUM_THREADS` 



