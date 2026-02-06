# 🏛️ APECS WhatsApp 

![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?style=flat-square)
![WhatsApp API](https://img.shields.io/badge/API-WhatsApp_Cloud-blue?style=flat-square)
![License](https://img.shields.io/badge/License-Proprietary-grey?style=flat-square)

## Descripción del Proyecto

Este repositorio contiene el código fuente del **Asistente Virtual de APECS**, una solución backend robusta construida con **Java** y **Spring Boot**.

El propósito de este sistema es automatizar la atención al estudiante y la difusión de información académica a través de WhatsApp. Diseñado bajo una **arquitectura determinista basada en reglas**, 
este chatbot garantiza la entrega de información crítica (horarios, costos, temarios y procesos de matrícula) con total precisión, inmediatez y disponibilidad 24/7, eliminando la ambigüedad en las respuestas.

## Arquitectura y Funcionamiento

El núcleo de la aplicación opera como un servicio RESTful que implementa el protocolo de **Webhooks** de Meta. Su flujo de trabajo se estructura de la siguiente manera:

1.  **Recepción Segura:** El controlador expone endpoints seguros que validan la firma digital de Meta y reciben los mensajes entrantes en formato JSON.
2.  **Motor de Decisión:** A diferencia de los modelos probabilísticos, este sistema utiliza un motor de lógica condicional (árboles de decisión) que procesa la entrada del usuario (texto)
3.  para determinar la ruta exacta de la conversación.
4.  **Gestión de Respuestas:** El servicio construye dinámicamente objetos de respuesta compatibles con la API de WhatsApp para guiar al usuario de forma intuitiva.

### Características Técnicas
* **Navegación Estructurada:** Menús interactivos que reducen el error del usuario.
* **Alta Concurrencia:** Basado en el modelo no bloqueante y eficiente de Spring Boot.
* **Escalabilidad:** Diseño modular que permite agregar nuevos flujos de conversación sin afectar la lógica existente.

---

##Guía de Instalación y Despliegue

Sigue estos pasos para ejecutar el entorno de desarrollo local.

### 1. Prerrequisitos
* **Java Development Kit (JDK) 17** o superior.
* **Maven** instalado (o uso del wrapper incluido).
* Una aplicación configurada en el portal **Meta for Developers** con el producto WhatsApp habilitado.
