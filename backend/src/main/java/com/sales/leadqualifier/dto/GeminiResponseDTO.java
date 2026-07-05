package com.sales.leadqualifier.dto;

import java.util.List;

/**
 * Data Transfer Object representing the response payload returned by the Google Gemini API.
 */
public class GeminiResponseDTO {

    private List<Candidate> candidates;

    public GeminiResponseDTO() {
    }

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    /**
     * Inner class representing individual candidates returned by Gemini.
     */
    public static class Candidate {
        private Content content;

        public Candidate() {
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }
    }

    /**
     * Inner class representing the container of response text parts.
     */
    public static class Content {
        private List<Part> parts;

        public Content() {
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }
    }

    /**
     * Inner class representing the text part holding Gemini's raw response.
     */
    public static class Part {
        private String text;

        public Part() {
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
