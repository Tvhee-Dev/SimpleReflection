# SimpleReflection

## Usage
1. To reflect on any object / class, create a new Reflection instance
```
Reflection myReflectedObject = new Reflection(myObject);
Reflection myReflectedClass = new Reflection(myClass);
```
2. You can create an instance of a class. If you are reflecting on an object, go to step 3
```
myReflecteedClass.instance(constructorArguments...);
```
3. Call any method from the Reflection class (field, method) to reflect. If no object instance is present, you have to create one (see 2) to change non-static fields too
4. If you are done, get the object back (any object you have gotten from fields/methods (step 3))
```
Object result = myReflectedObject.object();
```

## Important
1. It is impossible to reflect on Java's `record` classes. That is why this program will create a **new** instance from the class if you edit a field. The other fields will still have the same value
2. If you reflect from object -> object, do not forget to set immutable fields / record classes back! Example:
```
Reflection firstExample = new Reflection(object/class) //private final ExampleClass a;
Reflection a = firstExample.field("a"); //private final ExampleRecordClass b;
Reflection b = a.field("b"); //private final String exampleString = "";

b.field("exampleString", "aStringValue");
a.field("b", b.object());
firstExample.field("a", a);
```
