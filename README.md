# 🧠 Ollama Service — Spring AI Helper for Ollama Models

**Ollama Service** is a **Java 17** module built with **Spring Boot** and **Spring AI Starter Model Ollama**, designed to simplify working with Ollama-based LLMs in Spring applications.

This helper service provides an easy-to-use interface for **chat memory management**, **automatic chat history summarization**, and **tool configuration**. It also supports **dynamic model customization**—automatically creating and managing custom Ollama models from existing ones, letting you easily modify system prompts and templates without manual setup.

## ✨ Features
- 🧩 Built-in support for **Spring AI Ollama** integration  
- 🗂️ **Automatic chat history summarization** and **persistent memory**  
- ⚙️ **Custom Ollama model creation** with configurable system and template prompts  
- 🧠 Preconfigured for the **GPT OSS 20B** model (default)  
- 🧰 **Custom Tool Interface** — enables **dynamic tool name and description updates**, solving Spring AI’s limitation where tools are fixed via annotations  
- 🚀 Plug-and-play module — just clone, import, and call the provided functions  

## 🛠️ Tech Stack
- Java 17  
- Spring Boot  
- Spring AI Starter Model Ollama

## ⚡ Quick Start
1. Clone the repo:
   ```bash
   https://github.com/sharan245/OllamaService.git
