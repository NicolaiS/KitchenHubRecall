package kr.kaist.resl.kitchenhubrecall.models;

import java.util.Date;

/**
 * Model of recall notification from Recall service
 */

public class RecallNotification {

    private String recallSerial = null;
    private String urn = null;
    private Date issueDate = null;
    private String description = null;
    private String danger = null;
    private String instructions = null;
    private Integer version = null;


    public RecallNotification(String recallSerial, String urn,
                              Date issueDate, String description, String danger,
                              String instructions, Integer version) {
        this.recallSerial = recallSerial;
        this.urn = urn;
        this.issueDate = issueDate;
        this.description = description;
        this.danger = danger;
        this.instructions = instructions;
        this.version = version;
    }

    public String getRecallSerial() {
        return recallSerial;
    }

    public void setRecallSerial(String recallSerial) {
        this.recallSerial = recallSerial;
    }

    public String getUrn() {
        return urn;
    }

    public void setUrn(String urn) {
        this.urn = urn;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDanger() {
        return danger;
    }

    public void setDanger(String danger) {
        this.danger = danger;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
