package org.webpieces.templating.impl.tags;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.webpieces.ctx.api.Constants;
import org.webpieces.templating.api.HtmlTag;
import org.webpieces.templating.impl.GroovyTemplateSuperclass;
import org.webpieces.util.net.URLEncoder;

import com.google.common.collect.Sets;

import groovy.lang.Closure;

public class BootstrapModalTag implements HtmlTag {
	private Set<String> excludes = Sets.newHashSet("route", "modalId", "linkId");
	
// generates this.....
//
//	  $(document).ready(function() {	
//	         $("#editLink_1").click(function(e){
//	             $('#addEditPatternModal').load('/ajax/user/edit/999999', function(){
//	                 $("#addEditPatternModal").modal('show');	
//	              });
//	         });
//	   });
//	</script>
	@Override
	public void runTag(Map<Object, Object> args, Closure<?> body, PrintWriter out, GroovyTemplateSuperclass template, String srcLocation) {
        Object route = args.get("route");
        Object modalId = args.get("modalId");
        Object linkId = args.get("linkId");
        if(route == null)
        		throw new IllegalArgumentException("#{bootstrapModal/}# tag must contain a route argument like #{bootstrapModal route:@[AJAX_EDIT_USER_FORM, id:'{id}']@}#. "+srcLocation);
        else if(modalId == null)
        		throw new IllegalArgumentException("#{bootstrapModal/}# tag must contain a modalId argument like #{bootstrapModal modalId:'addEditModal'}#. "+srcLocation);
        else if(linkId == null)
        		throw new IllegalArgumentException("#{bootstrapModal/}# tag must contain a linkId argument like #{bootstrapModal linkId:'edit_'+entity.id}#. "+srcLocation);
        else if(body != null)
        		throw new IllegalArgumentException("Only #{bootstrapModal/}# can be used.  You cannot do #{bootstrapModal}# #{/bootstrapModal} as the body is not used with this tag"+srcLocation);

        String urlPath = fillInUrlParams(route+"", args);
        printXX(out, "<script type=`text/javascript`>");
        println(out, "  $(document).ready(function() {");	
        printXX(out, "         $(`#"+linkId+"`).click(function(e){");
        println(out, "             $('#"+modalId+"').load('"+urlPath+"', function(response, status, xhr){");
        println(out, "                 if (xhr.status == "+Constants.AJAX_REDIRECT_CODE+") {");
        println(out, "                     window.location = xhr.getResponseHeader('Location')");
        println(out, "                 } else if (xhr.status != 200) {");
        println(out, "                     alert('Cannot connect to server.  Check your network connection')");
        println(out, "                 } else {");
        printXX(out, "                     $(`#"+modalId+"`).modal('show');");
        println(out, "                 }");
        println(out, "              });");
        println(out, "         });");
        println(out, "   });");
        println(out, "</script>");
	}

	private String fillInUrlParams(String route, Map<Object, Object> args) {
		String modifiedRoute = URLEncoder.decode(route, StandardCharsets.UTF_8);
		for(Entry<Object, Object> entry : args.entrySet()) {
			String key = entry.getKey()+"";
			if(excludes.contains(key))
				continue;
			
			String value = entry.getValue()+"";
			String encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
			modifiedRoute = modifiedRoute.replace("{"+key+"}", encodedValue);
		}
		
		return modifiedRoute;
	}

	private void println(PrintWriter out, String string) {
		out.println(string);
	}

	private void printXX(PrintWriter out, String string) {
		out.println(string.replace("`", "\""));
	}

	@Override
	public String getName() {
		return "bootstrapModal";
	}
}
