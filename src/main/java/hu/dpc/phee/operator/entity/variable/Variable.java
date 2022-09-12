package hu.dpc.phee.operator.entity.variable;


import hu.dpc.phee.operator.entity.parent.AbstractPersistableCustom;
import org.eclipse.persistence.annotations.Index;

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "variables")
@Cacheable(false)
public class Variable extends AbstractPersistableCustom<Long> {

    @Column(name = "WORKFLOW_KEY")
    private Long workflowKey;

    @Column(name = "WORKFLOW_INSTANCE_KEY")
    @Index(name = "idx_workflowInstanceKey")
    private Long workflowInstanceKey;

    @Column(name = "TIMESTAMP")
    private Long timestamp;

    @Column(name = "NAME")
    private String name;

    @Lob
    @Column(name = "VALUE")
    private String value;

    public Long getWorkflowKey() {
        return workflowKey;
    }

    public void setWorkflowKey(Long workflowKey) {
        this.workflowKey = workflowKey;
    }

    public Long getWorkflowInstanceKey() {
        return workflowInstanceKey;
    }

    public void setWorkflowInstanceKey(Long workflowInstanceKey) {
        this.workflowInstanceKey = workflowInstanceKey;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
