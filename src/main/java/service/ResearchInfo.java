package service;

import java.util.HashMap;
import java.util.List;

public class ResearchInfo {

    private int research_id;
    private String research_name;
    private HashMap<String, String> questions;
    private String status;

    public int getResearch_id() {
        return research_id;
    }

    public HashMap<String, String> getQuestions() {
        return questions;
    }

    public String getStatus() {
        return status;
    }

    public String getResearch_name() {
        return research_name;
    }

    public ResearchInfo(int research_id, String name, HashMap<String, String> questions, String status) {
        this.research_id = research_id;
        research_name = name;
        this.questions = questions;
        this.status = status;
    }

    public ResearchInfo(){
        research_id = 0;
        research_name = "";
        questions = new HashMap<>();
        status = "";
    }

    public void setResearch_id(int research_id) {
        this.research_id = research_id;
    }

    public void setResearch_name(String research_name) {
        this.research_name = research_name;
    }

    public void setQuestions(HashMap<String, String> questions) {
        this.questions = questions;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
