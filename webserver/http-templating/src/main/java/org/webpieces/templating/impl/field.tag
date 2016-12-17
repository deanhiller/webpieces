<div id="${field.id}$_div" class="control-group ${field.errorClass}$">
        <label class="control-label">
           *{ This is for tooltip popup }*
           <a href="#" rel="tooltip" title="&{'', field.i18nKey+'.help'}&"><i class="icon-info-sign"></i></a>
           *{i18n fields are default text then key so read the label if it exists or lookup by key name}*
           &{optional('label'), field.i18nKey}&
        </label>
        <div class="controls">
            ${_body}$
            <span id="${field.id}$_errorMsg" class="errorMsg">${field.error}$</span>
        </div>
</div>