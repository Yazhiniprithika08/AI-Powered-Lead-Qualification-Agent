package com.sales.leadqualifier.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object representing the request payload for Google Gemini API.
 * Configured to demand structured JSON responses.
 */
public class GeminiRequestDTO {

    private List<Content> contents;
    private GenerationConfig generationConfig;

    public GeminiRequestDTO() {
        this.contents = new ArrayList<>();
    }

    /**
     * Parameterized constructor helper to build a request with a prompt.
     * Sets responseMimeType to application/json to enforce structured output.
     */
    public GeminiRequestDTO(String promptText) {
        this.contents = new ArrayList<>();
        Content content = new Content();
        content.getParts().add(new Part(promptText));
        this.contents.add(content);
        this.generationConfig = new GenerationConfig("application/json");
    }

    // Getters and Setters
    public List<Content> getContents() {
        return contents;
    }

    public void setContents(List<Content> contents) {
        this.contents = contents;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public void setGenerationConfig(GenerationConfig generationConfig) {
        this.generationConfig = generationConfig;
    }

    /**
     * Inner class representing the contents property in Gemini request.
     */
    public static class Content {
        private List<Part> parts;

        public Content() {
            this.parts = new ArrayList<>();
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    /**
     * Inner class representing the part containing the actual text.
     */
    public static class Part {
        private String text;

        public Part() {
        }

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * Inner class configuring generation parameters like output mime type.
     */
    public static class GenerationConfig {
        private String responseMimeType;

        public GenerationConfig() {
        }

        public GenerationConfig(String responseMimeType) {
            this.responseMimeType = responseMimeType;
        }

        public String getResponseMimeType() {
            return responseMimeType;
        }

        public void setResponseMimeType(String responseMimeType) {
            this.responseMimeType = responseMimeType;
        }
    }
}
