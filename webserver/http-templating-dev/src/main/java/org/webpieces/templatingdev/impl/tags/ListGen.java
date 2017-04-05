package org.webpieces.templatingdev.impl.tags;

import javax.inject.Inject;

import org.webpieces.templatingdev.api.ScriptOutput;
import org.webpieces.templatingdev.api.Token;

public class ListGen extends ParseTagArgs {

	@Inject
	public ListGen(RoutePathTranslator callback) {
		super(callback);
	}

	@Override
	public String getName() {
		return "list";
	}

	@Override
	public void generateStart(ScriptOutput sourceCode, Token token, int uniqueId) {
		super.generateStartAttrs(sourceCode, token, uniqueId);
		
		String tagBody = token.getCleanValue();
		if(!tagBody.contains("items:"))
			throw new IllegalArgumentException("#{list}# tag must have 'items:' attribute. "+token.getSourceLocation(true));			
		else if(!tagBody.contains("items:"))
			throw new IllegalArgumentException("#{list}# tag must have 'as:' attribute. "+token.getSourceLocation(true));
		
		String srcLocation = token.getSourceLocation(false);
		StringBuilder s = new StringBuilder();
        s.append("if(!_attrsXXX['as']) {\n");
        s.append("    throw new IllegalArgumentException('Missing \"as\" argument or its value is null. "+srcLocation+"');\n");
        s.append("};\n");
        s.append("if(!_attrsXXX['items']) {\n");
        //s.append("    throw new IllegalArgumentException('Missing \"items\" argument. val='+_attrsXXX+' "+srcLocation+"');\n");
        s.append("}\n");
        s.append("hasItemsXXX = false;\n");
        s.append("if(_attrsXXX['items']) {\n");
        s.append("   _iterXXX = _attrsXXX['items'].iterator();\n");
        s.append("   for (_iXXX = 0; _iterXXX.hasNext(); _iXXX++) {\n");
        s.append("      hasItemsXXX = true;\n");
        s.append("      _itemXXX = _iterXXX.next();\n");
        s.append("      setProperty(_attrsXXX['as'], _itemXXX);\n");
        s.append("      setProperty(_attrsXXX['as']+'_index', _iXXX);\n");
        s.append("      setProperty(_attrsXXX['as']+'_isLast', !_iterXXX.hasNext());\n");
        s.append("      setProperty(_attrsXXX['as']+'_isFirst', _iXXX == 1);\n");
        s.append("      setProperty(_attrsXXX['as']+'_parity', _iXXX%2==0?'even':'odd');\n");
        
        String result = s.toString().replaceAll("XXX", ""+uniqueId);
        
        sourceCode.println(result, token);
		sourceCode.println();
		
		sourceCode.println("      _body" + uniqueId + " = {", token);
		sourceCode.println();
	}

	@Override
	public void generateEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		String srcLocation = token.getSourceLocation(false);
		sourceCode.println("      };", token); //close _body closure
		sourceCode.println("      String bodyStr = runClosure('list', _body"+uniqueId+", '"+srcLocation+"');", token);
		sourceCode.println("      __out.print(bodyStr);", token);
		sourceCode.println();
		
		sourceCode.println("   }", token); //close for loop
		sourceCode.println("}", token); //close if statement
		
		//This is only here so it connects to the #{else} tag seamlesslessly 
		//as in if(hasItems){} else {.....} so the else tag works with the list tag
		sourceCode.println("if(hasItems"+uniqueId+") {", token);  
		sourceCode.println("}", token);
		sourceCode.println();
	}

	@Override
	public void generateStartAndEnd(ScriptOutput sourceCode, Token token, int uniqueId) {
		throw new UnsupportedOperationException("#{list}# tag must have body and didn't. "+token.getSourceLocation(true));
	}

}
