package org.webpieces.gradle.compiler;

import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;

public class TemplateCompileOptions {
    private String encoding = "UTF-8";
	private Set<String> customTags;
    
	@Inject
	public TemplateCompileOptions(Project project) {
	}
	
    @Optional @Input
    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    
    public Set<String> getCustomTags() {
    	return customTags;
    }

	public void setCustomTags(Set<String> customTags) {
		this.customTags = customTags;
	}
    
}
