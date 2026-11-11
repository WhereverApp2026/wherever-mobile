# Reglas propias de R8 (specs/nv-41).
#
# No hacen falta reglas para el stack: Retrofit 3, OkHttp 5, kotlinx.serialization 1.12,
# Hilt, Room y Compose traen sus reglas de consumidor dentro del AAR/JAR.
# Los DTO son @Serializable: el plugin genera `serializer()` y las reglas de
# kotlinx.serialization lo conservan. Retrofit conserva las interfaces con anotaciones HTTP
# y la firma genérica de las funciones suspend.
#
# Si un DTO deja de llegar en release (campos vacíos o SerializationException),
# primero comprobar que la clase tiene @Serializable; no añadir -keep de paquetes completos.

# Trazas legibles en los informes de fallos, sin exponer el nombre del archivo fuente.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
