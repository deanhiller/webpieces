package org.webpieces.elasticsearch.queries;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class Suggestion {

    //For completion mappings, use these 2 fields for querying
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String prefix;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Completion completion;

    //for other use these
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String text;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer offset;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer length;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Option> options;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Term term;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Completion getCompletion() {
        return completion;
    }

    public void setCompletion(Completion completion) {
        this.completion = completion;
    }
}
