# 🐄 Sistema de Gestión Ganadera

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)

El Sistema Integral de Gestión Ganadera es una aplicación móvil Android nativa diseñada para optimizar la administración de ranchos en Dolores Hidalgo. 
Su objetivo es sustituir los registros manuales tradicionales por un sistema digital que permita llevar un control preciso de cada animal, gestionar 
información sanitaria y generar reportes analíticos que faciliten la toma de decisiones basadas en datos confiables.

La aplicación incorpora un sistema de autenticación seguro, donde los usuarios pueden registrarse e iniciar sesión mediante correo electrónico y contraseña. 
Se distinguen dos perfiles principales: los propietarios de rancho, quienes cuentan con permisos de administrador y pueden generar códigos de invitación para agregar empleados, 
y los empleados, que acceden al sistema mediante dichos códigos y reciben roles específicos según sus funciones.

Los roles de empleados incluyen veterinarios, con acceso completo al historial médico y capacidad de registrar tratamientos; supervisores, 
que pueden dar de alta animales, actualizar información y consultar reportes; y empleados generales, con acceso limitado principalmente de lectura. 
Todo el sistema de permisos se gestiona en tiempo real mediante Firebase, lo que asegura que cualquier modificación realizada por el administrador se 
refleje de inmediato en la cuenta de los empleados sin necesidad de reiniciar sesión.


## 📱 Características Principales

### 🔐 Autenticación y Gestión de Usuarios
- Registro e inicio de sesión con email/contraseña
- Recuperación de contraseña
- Sistema de códigos de invitación personalizables
- Gestión de sesiones segura con Firebase Authentication

### 🏢 Gestión Multi-Usuario por Rancho
- Registro de propietarios de rancho con permisos completos
- Sistema de invitaciones por código alfanumérico (6-12 caracteres)
- Sincronización automática entre usuarios del mismo rancho
- Aprobación y gestión de empleados

### 👥 Sistema de Roles y Permisos
- **Propietario/Admin**: Control total del rancho y gestión de usuarios
- **Veterinario**: Acceso completo a historial médico y registros de salud
- **Supervisor**: Registro de animales, actualización de pesos y reportes
- **Empleado**: Acceso de lectura con permisos limitados de edición
- **Actualización en tiempo real**: Los cambios de permisos se reflejan instantáneamente

### 🐮 Gestión Integral de Animales
- Registro detallado con número de arete autogenerado
- Información genealógica (madre y padre)
- Datos físicos: peso, fecha de nacimiento, raza (con autocompletado)
- Fotografías de cada animal
- Búsqueda y filtrado por arete o nombre
- Sincronización en tiempo real con Firebase Firestore

### 💉 Historial Médico y Sanitario
- Registro de vacunaciones, desparasitaciones y tratamientos
- Sistema de estados: Pendiente → Realizado
- Historial completo por animal con fechas y responsables
- Filtros por tipo de tratamiento y estado
- Notas y observaciones detalladas

### 📊 Control de Peso y Análisis
- Registro periódico de pesajes con fecha y observaciones
- Gráficas de evolución temporal del peso
- Estadísticas automáticas: peso actual, promedio, tendencias
- Visualización interactiva con Vico Charts
- Detección de patrones de crecimiento

### 📈 Dashboard Analítico
- Contadores en tiempo real: Total de animales, vacas, toros, becerros
- Registros recientes de animales agregados
- Distribución por raza y tipo
- Acceso rápido a funcionalidades principales
- Actualización automática al sincronizar con Firebase

### 📄 Generación de Reportes
- Exportación de inventario completo a PDF
- Listado detallado de todos los animales
- Distribución por raza con estadísticas
- Resumen de peso promedio por tipo
- Reportes profesionales listos para compartir

### 🎨 Interfaz de Usuario Moderna
- Material Design 3 con Jetpack Compose
- Tema personalizado con paleta de colores profesional
- Navegación fluida entre pantallas
- Animaciones y transiciones suaves
- Diseño adaptable y responsive

## 🛠️ Tecnologías Utilizadas

### Lenguaje y Framework
- **Kotlin** - Lenguaje de programación principal
- **Jetpack Compose** - Framework declarativo para UI moderna
- **Coroutines & Flow** - Programación asíncrona reactiva

### Arquitectura
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern** - Abstracción de fuentes de datos
- **StateFlow** - Gestión de estado reactiva
- **Single Activity Architecture** - Navegación con Compose

### Persistencia de Datos
- **Room Database** - Base de datos local SQLite
- **Firebase Firestore** - Base de datos en tiempo real NoSQL
- **Firebase Authentication** - Gestión de autenticación

### Bibliotecas Principales
- **Material 3 Components** - Componentes UI de última generación
- **Coil** - Carga y caché de imágenes eficiente
- **Vico Charts** - Gráficas interactivas y visualización de datos
- **iText PDF** - Generación de documentos PDF
- **Navigation Compose** - Sistema de navegación declarativa

## 📁 Estructura del Proyecto
```
app/src/main/java/mx/edu/utng/lojg/ganaderia20/
├── data/
│   ├── dao/                    # Acceso a datos Room
│   │   ├── AnimalDao.kt
│   │   └── RegistroSaludDao.kt
│   ├── entities/               # Entidades de base de datos
│   │   ├── AnimalEntity.kt
│   │   ├── RegistroSaludEntity.kt
│   │   ├── User.kt
│   │   └── Permission.kt
│   └── AppDatabase.kt          # Configuración de Room
├── Repository/                 # Repositorios de datos
│   ├── AnimalRepository.kt
│   ├── AuthRepository.kt
│   ├── CodigoInvitacionRepository.kt
│   └── RegistroSaludRepository.kt
├── viewmodel/                  # Lógica de presentación
│   ├── GanadoViewModel.kt
│   ├── AuthViewModel.kt
│   └── AuthViewModelFactory.kt
├── ui/theme/
│   ├── screens/               # Pantallas principales
│   │   ├── LoginScreen.kt
│   │   ├── RegistroScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── PantallaMisAnimales.kt
│   │   ├── PantallaRegistrarCria.kt
│   │   ├── HistorialSaludScreen.kt
│   │   ├── HistorialSaludGeneralScreen.kt
│   │   ├── RegistrarSaludScreen.kt
│   │   ├── ActualizarPesoScreen.kt
│   │   ├── HealthReportScreen.kt
│   │   ├── ReportsScreen.kt
│   │   ├── ConfiguracionScreen.kt
│   │   └── UsuariosScreen.kt
│   └── components/            # Componentes reutilizables
│       ├── DashboardCard.kt
│       ├── TarjetaAnimal.kt
│       ├── TarjetaSalud.kt
│       ├── AnimalSaludCard.kt
│       └── CustomTextField.kt
├── navigation/                # Sistema de navegación
│   └── NavGraph.kt
├── models/                    # Modelos de datos
│   ├── Animal.kt
│   ├── CodigoInvitacion.kt
│   ├── FormularioRegistro.kt
│   ├── ResultadoRegistro.kt
│   ├── InventoryItem.kt
│   └── BreedDistribution.kt
└── utils/                     # Utilidades
    └── ExportUtils.kt
```

## 🚀 Instalación y Configuración

### Requisitos Previos
- Android Studio Hedgehog o superior
- JDK 17+
- Cuenta de Firebase
- Dispositivo Android 7.0+ (API 24+) o emulador

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/README.md
cd ganaderia20
```

2. **Configurar Firebase**
   - Crear un proyecto en [Firebase Console](https://console.firebase.google.com/)
   - Habilitar Authentication (Email/Password)
   - Crear base de datos Firestore
   - Descargar `google-services.json`
   - Colocar el archivo en `app/google-services.json`

3. **Configurar Firestore Security Rules**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isSignedIn() {
      return request.auth != null;
    }
    
    match /usuarios/{userId} {
      allow read: if isSignedIn();
      allow write: if isSignedIn() && request.auth.uid == userId;
    }
    
    match /animales/{animalId} {
      allow read, write: if isSignedIn();
    }
    
    match /codigos_invitacion/{codeId} {
      allow read, write: if isSignedIn();
    }
    
    match /notificaciones_permisos/{userId} {
      allow read, write: if isSignedIn();
    }
  }
}
```

4. **Configurar dependencias en build.gradle**

Asegúrate de tener estas dependencias en `build.gradle (Module: app)`:
```gradle
dependencies {
    // Jetpack Compose
    implementation platform('androidx.compose:compose-bom:2024.02.00')
    implementation 'androidx.compose.ui:ui'
    implementation 'androidx.compose.material3:material3'
    implementation 'androidx.compose.ui:ui-tooling-preview'
    
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-auth-ktx'
    implementation 'com.google.firebase:firebase-firestore-ktx'
    
    // Room Database
    implementation "androidx.room:room-runtime:2.6.1"
    implementation "androidx.room:room-ktx:2.6.1"
    kapt "androidx.room:room-compiler:2.6.1"
    
    // Navigation Compose
    implementation "androidx.navigation:navigation-compose:2.7.6"
    
    // Coil para imágenes
    implementation "io.coil-kt:coil-compose:2.5.0"
    
    // Vico Charts
    implementation "com.patrykandpatrick.vico:compose:1.13.1"
    implementation "com.patrykandpatrick.vico:compose-m3:1.13.1"
    
    // iText PDF
    implementation 'com.itextpdf:itext7-core:7.2.5'
}
```

5. **Sincronizar y compilar**
```bash
# En Android Studio:
File → Sync Project with Gradle Files
Build → Clean Project
Build → Rebuild Project
```

6. **Ejecutar la aplicación**
```bash
Run → Run 'app'
```

## 📸 Capturas de Pantalla

### Autenticación
<p align="center">
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/Login.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/CreacionCuenta.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/CreacionCuenta2.jpg" width="200" />
</p>

### Dashboard y Gestión de Animales
<p align="center">
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/Dashboard.jpg" width="200" />
   <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/RegistrarCria.jpg" width="200" /> 
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/MisAnimales.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/FiltroMisAnimales.jpg" width="200" />
</p>

### Historial Médico y Reportes
<p align="center">
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/RegistroSalud.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/HistorialGeneralSalud.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/ReporteSaludAnimal.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/ReportesGenerales.jpg" width="200" />
</p>

### Administración
<p align="center">
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/AdministracionUsuarios.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/CreacionCodigoInvitaciom.jpg" width="200" />
    <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/CodigoGenerado.jpg" width="200" />  
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/ConfiguracionCuenta.jpg" width="200" />
  <img src="https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/GanaderiaDocs/ConfiguarcionCuenta2.jpg" width="200" />
</p>

## 🎯 Casos de Uso

### Escenario 1: Registro de Nuevo Animal
1. **Usuario autorizado** abre la app y navega a "Registrar Cría"
2. El sistema genera automáticamente el siguiente número de arete
3. Completa información: nombre, tipo, raza, peso, genealogía
4. (Opcional) Toma fotografía del animal
5. Confirma el registro
6. El animal se sincroniza con Firebase y aparece para todos los usuarios del rancho

### Escenario 2: Control de Vacunación
1. **Veterinario** selecciona un animal desde "Mis Animales"
2. Accede a "Registrar Evento de Salud"
3. Selecciona tipo: "Vacunación"
4. Ingresa detalles: vacuna aplicada, dosis, fecha
5. Marca como "Pendiente" si requiere seguimiento
6. El registro queda en el historial médico del animal
7. Otros usuarios pueden ver el estado actualizado

### Escenario 3: Análisis de Crecimiento
1. **Supervisor** selecciona un animal
2. Navega a "Ver Reporte"
3. Ve la gráfica de evolución de peso temporal
4. Revisa estadísticas: peso actual, promedio, última revisión
5. Decide actualizar peso desde "Actualizar Peso"
6. Ingresa nuevo peso y observaciones
7. El sistema actualiza automáticamente las gráficas

### Escenario 4: Gestión de Equipo
1. **Propietario** genera código de invitación desde Configuración
2. Selecciona tipo: "Veterinario", usos: 1, longitud: 8
3. Comparte el código generado con el nuevo empleado
4. Empleado se registra e ingresa el código
5. El sistema asigna permisos de veterinario automáticamente
6. Empleado ya puede acceder a todos los animales del rancho

### Escenario 5: Generación de Reportes
1. **Admin** navega a "Informe General"
2. Ve resumen de inventario por tipo y raza
3. Selecciona "Descargar PDF"
4. El sistema genera reporte profesional con:
   - Listado completo de animales
   - Distribución por raza
   - Estadísticas de peso
5. Comparte el PDF por correo o WhatsApp

## 🔒 Seguridad

- ✅ Autenticación segura con Firebase
- ✅ Reglas de seguridad en Firestore por usuario
- ✅ Validación de permisos por rol en tiempo real
- ✅ Datos sensibles almacenados de forma encriptada
- ✅ Sistema de códigos de invitación con expiración
- ✅ Protección contra acceso no autorizado a datos de otros ranchos
- ✅ Sincronización segura entre Room y Firebase

## 📝 Base de Datos

### Base de Datos Local (Room)

#### Tabla: `animal`
```kotlin
data class AnimalEntity(
    val id: Int,                    // Primary Key
    val arete: String,              // Número único de identificación
    val nombre: String,             // Nombre del animal
    val tipo: String,               // Vaca, Toro, Becerro
    val raza: String,               // Raza del animal
    val fechaNacimiento: String,    // dd/MM/yyyy
    val peso: String,               // Peso en kg
    val madre: String?,             // Arete de la madre
    val padre: String?,             // Arete del padre
    val observaciones: String?,     // Notas adicionales
    val estadoSalud: String,        // Estado general
    val foto: String?,              // URI de la foto
    val usuarioId: String,          // ID del usuario que registró
    val adminId: String,            // ID del propietario del rancho
    val registradoPor: String?      // Nombre de quien registró
)
```

#### Tabla: `registro_salud`
```kotlin
data class RegistroSaludEntity(
    val id: Int,                    // Primary Key
    val areteAnimal: String,        // Foreign Key a animal
    val fecha: String,              // Fecha del evento
    val tipo: String,               // Vacunación, Desparasitación, etc.
    val tratamiento: String,        // Descripción del tratamiento
    val responsable: String,        // Veterinario/responsable
    val observaciones: String,      // Notas adicionales
    val estado: String              // Pendiente/Realizado
)
```

### Base de Datos Cloud (Firestore)

#### Colección: `usuarios`
```json
{
  "uid": "string",
  "username": "string",
  "email": "string",
  "nombre": "string",
  "telefono": "string",
  "rol": "string",
  "permisos": ["array"],
  "adminId": "string",
  "esDuenoRancho": "boolean",
  "fechaRegistro": "timestamp",
  "activo": "boolean",
  "ultimoAcceso": "timestamp",
  "codigoInvitacionUsado": "string"
}
```

#### Colección: `animales`
```json
{
  "arete": "string",
  "nombre": "string",
  "tipo": "string",
  "raza": "string",
  "fechaNacimiento": "string",
  "peso": "string",
  "madre": "string",
  "padre": "string",
  "observaciones": "string",
  "estadoSalud": "string",
  "foto": "string",
  "usuarioId": "string",
  "adminId": "string",
  "registradoPor": "string",
  "fechaRegistro": "timestamp"
}
```

#### Colección: `codigos_invitacion`
```json
{
  "id": "string",
  "codigo": "string",
  "adminId": "string",
  "tipo": "string",
  "activo": "boolean",
  "fechaCreacion": "timestamp",
  "fechaExpiracion": "timestamp",
  "usadoEl": "timestamp",
  "usadoPor": "string",
  "usosRestantes": "number",
  "usosTotales": "number"
}
```

## 🤝 Contribuciones

Las contribuciones son bienvenidas. Para cambios importantes:

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/NuevaFuncionalidad`)
3. Commit tus cambios (`git commit -m 'Add: Nueva funcionalidad de...'`)
4. Push a la rama (`git push origin feature/NuevaFuncionalidad`)
5. Abre un Pull Request

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## 👨‍💻 Autor

**Diana Mabel García Martínez**
**Luis Owen Jaramillo Guerrero**
- GitHub: [@tu-usuario](https://github.com/diabegarcia-coder)
- Email: diabegarciamtz@gmail.com

## 🙏 Agradecimientos

- Universidad Tecnológica del Norte de Guanajuato
- Profesor: Anastacio Rodriguez García
- Firebase y Google por las herramientas de desarrollo
- Comunidad de Jetpack Compose y Android Developers

## 📚 Recursos Adicionales

- [Documentación de Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Documentación de Firebase](https://firebase.google.com/docs)
- [Guía de Room Database](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)


## ⚜️ ACTIVIDADES EXTRAS ⚜️

| Actividad | Evidencias | CAMPO DE GOLF |
| :--- | :--- | :--- |
| Actividad 2: Demostración Funcional | [Evidencia](#) | [Ver ejercicio](#) |
| Actividad 3: Pruebas con Usuarios | [Evidencia](#) | [Ver ejercicio](#) |
| Carpeta docs/imágenes | [Evidencias](#) | [Ver ejercicio](#) |
| Enlace a código documentado | [Evidencias](#) | [Ver ejercicio 1](https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/app/src/main/java/mx/edu/utng/lojg/ganaderia20/ui/theme/screens/LoginScreen.kt) [Ver ejercicio 2](https://github.com/diabegarciamtz-coder/Ganaderia2/blob/master/app/src/main/java/mx/edu/utng/lojg/ganaderia20/MainActivity.kt) |


---

## 🐄 Sobre el Proyecto 

Este sistema fue desarrollado como proyecto para la materia de Aplicaciones móviles con el objetivo de digitalizar y modernizar la gestión ganadera en Dolores HIdalgo y esperando que tenga crecimiento a todo México, facilitando el trabajo de propietarios y empleados de ranchos mediante tecnología móvil accesible y eficiente.

---
