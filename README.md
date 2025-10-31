# 🧠 Ollama Service — Spring AI Helper for Ollama Models

**Ollama Service** is a **Java 17** module built with **Spring Boot**, **Spring AI Starter Model Ollama**, and **Spring AI Starter MCP Client**, designed to simplify working with Ollama-based LLMs in Spring applications.

It provides an easy-to-use interface for **chat memory management**, **automatic chat history summarization**, and **tool configuration**, along with **dynamic model customization**—allowing easy editing of system prompts and templates without manual setup.

## 🏗️ Project Structure
- **'ollama-connect'** — Main module containing all core functionality: summarization, memory, custom tool support, and model management.   **you can add 'ollama-connect' module to your project and start using all the features.**  


- **'sample'** — Sample module showcasing how to use `ollama-connect`.  
  - The `main` method contains a `public CommandLineRunner test()` method with sample API calls you can directly run and play with.  
  - For summarization, see `SummarizeHistoryServices` class.  
  - Custom tool implementations are in the `tool` folder.  

## ✨ Features
- 🧩 **Spring AI Ollama** integration  
- 🗂️ **Automatic chat history summarization** and persistent memory  
- ⚙️ **Custom Ollama model creation** with editable system and template prompts  
- 🧠 Preconfigured for the **GPT OSS 20B** model (default)  
- 🧰 **Custom Tool Interface** — dynamically update tool names and descriptions, solving annotation-based limitations in Spring AI  
- 🚀 Plug-and-play — just include `ollama-connect` in your project  

## 🛠️ Tech Stack
- Java 17  
- Spring Boot  
- Spring AI Starter Model Ollama  
- Spring AI Starter MCP Client  

## 🛠️ Sample Module — Build, Run & Test

This README covers how to **build and run the sample module**, configure your environment, and test the provided APIs.

## ⚡ Build & Run the Sample Module
1. Clone the repository if you haven't already:
   ```bash
   git clone https://github.com/yourusername/ollama-service.git

2. Build the project and generate the sample JAR:
   ```bash
   ./mvnw clean install
   
3. Run the sample module:
   ```bash
   java -jar sample/target/sample-0.0.1-SNAPSHOT.jar
   
The Spring Boot application will start and run at `http://localhost:8080`

## 🛠️ Configure Your Environment
- Add your **Ollama module host URL** in `application-ollama.yml.`
- Configure your **MongoDB connection** in `application-database.yml.`

## 📡 Sample APIs
- **Create New Conversation**
  ```bash
  curl -X POST http://localhost:8080/message/createNewConversation
  
- **Continue Conversation**
  ```bash
  curl -X POST http://localhost:8080/message/continueConversation \
     -H "Content-Type: application/json" \
     -d '{ "conversationId": "YOUR_ID", "message": "Hello Ollama!" }'

- **Open Older Conversation**
  ```bash
  curl -X POST http://localhost:8080/message/openOlderConversation \
     -H "Content-Type: application/json" \
     -d '{ "conversationId": "YOUR_ID" }'

## 💡 Notes

- The sample module demonstrates usage of the ollama-connect module.
- The main method in the sample module contains a public CommandLineRunner test() method with example API calls you can directly run and play with.
- For summarization, check the SummarizeHistoryServices class.
- Custom tool implementations are located in the tool folder.
- You only need the ollama-connect module in your project — it provides chat memory, summarization, custom tools, and all core functionality needed to build an Ollama-based app.


