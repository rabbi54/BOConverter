
# BOConverter

## About BOConverter

BOConverter is a powerful and easy-to-use Java utility designed for converting objects to bytes and vice versa. It streamlines the serialization and deserialization process, making it both efficient and less prone to errors. Whether you're working with simple data types or complex nested objects, BOConverter provides a robust solution for handling binary data in your Java applications.

---

## Key Features

- **Simple Usage**: Just annotate your object properties to specify how they should be serialized.
- **Nested Object Support**: Automatically handles nested objects without requiring extra code.
- **Array Support**: Supports serialization of arrays of simple objects like `long`, `float`, `double`, and other supported types.
- **Reduced Boilerplate Code**: Eliminates the need to write custom parsers for each class type, reducing the potential for errors.

### Serializers
- `IntegerSerializer`: Serializes an integer in **`LITTLE_ENDIAN`** order, using 4 bytes.
- `UUIDSerializer`: Serializes datauuid of any objects.
- `StringSerializer`: It supports both fixed sized string and variable sized string. If `length` is not provided as annotation the it treats string as varaible sized and add length as prefix.
- `DoubleSerializer`: Converts double type variable using 8 byte length.
- `ArraySerializer`: Requires specifying the `innerType` to serialize the objects within the array and length will be added as prefix.
- `BooleanSerializer`: Serializes the boolean objects.
- `ByteIntSerializer`: Serializes an integer into a single byte.
- `ShortSerializer`: Serializes an short variable.
- `FloatSerializer`: Serializes a float value into 4 bytes, manually handling byte order. Deserializes from 4 bytes back to a float, ensuring the value is neither NaN nor infinite.
- `LongSerializer`: Serializes an long in **`LITTLE_ENDIAN`** order, using 8 bytes.
- `LongFrom4ByteSerializer`: Serializes an long in **`LITTLE_ENDIAN`** order, using 4 bytes.
- `LocationDataSerializer`: Serializes a double value into 8 bytes by manually converting the double to its hexadecimal representation. Deserializes from 8 bytes back to a double, ensuring the value is neither NaN nor infinite.
- `TimeSerializer`: Serializes a long value representing time into 4 bytes by converting it to seconds since January 1, 1990, and then to a hexadecimal string. Deserializes the 4 bytes back into the original long value, adjusting for the time offset.


## Usage

### 1. Annotation

To use the converter, annotate your object properties with the `@ByteSerialize` annotation:

```java
@ByteSerialize(type = YourSerializerClass.class, identifier = someByte, length = someLength, required = true/false, innerType = InnerClassSerializer.class)
private YourType yourField;
```

### 2. Annotation Parameters

- **`type`**: Specifies the serializer class to be used for this field.
- **`identifier`**: A unique byte value that identifies this field during serialization/deserialization.
- **`length`**: The length of the serialized data. If not provided, the converter will automatically add the length of the byte representation as a prefix.
- **`required`**: Specifies whether serialization is mandatory, even if the value is null. If set to true and the value is null, it will be replaced by a default value during serialization.
- **`innerType`**: Specifies the type of the elements when the `type` is `ArraySerializer.class`. Only needed for arrays.

**Note**: If you don't provide the `type`, the field will be treated as a nested object. The `innerType` is only required for arrays. If you don't provide the `length` it means the field is variable length. By default `required` is set to true.


Here’s a more generic example for the serialization and deserialization sections:

### 3. Serialization Example

```java
ObjectSerializer objectSerializer = new ObjectSerializer();

// Serialize an object to a byte array
byte[] serializedData = objectSerializer.serialize(yourObject);
```

### 4. Deserialization Example

```java
// Deserialize the byte array back to the object
YourClass deserializedObject = (YourClass) objectSerializer.deserialize(serializedData, YourClass.class);
```

This example uses `yourObject` and `YourClass` as placeholders to represent any object and its corresponding class.


## Advantages

- **Rapid Development**: With the annotation-based approach, you can quickly set up serialization and deserialization for complex objects, speeding up development time.
- **Ease of Use**: The intuitive annotation system makes it easy to understand and implement, even for developers new to serialization.
- **Code Reduction**: No need to manually write parsers for each object type, resulting in less code to maintain.
- **Error Reduction**: The automated handling of serialization/deserialization reduces the likelihood of errors, leading to more reliable code.
- **Consistency**: The converter ensures consistent serialization and deserialization processes across different objects, reducing potential discrepancies.
- **Flexibility**: The ability to support nested objects and custom serializers allows for a wide range of use cases without significant changes to the codebase.
- **Maintainability**: By centralizing serialization logic, the converter makes it easier to maintain and update as the codebase evolves.

## Drawbacks

- **Limited Array Support**: Currently, the converter only supports arrays of simple objects like `long`, `float`, `double`, and other supported types. Complex or nested arrays are not supported.

---

By following the above instructions, you can easily serialize and deserialize Java objects without needing to write extensive custom code. This converter is designed to be simple and efficient, reducing both development time and potential for errors while providing flexibility and maintainability for complex object serialization.
