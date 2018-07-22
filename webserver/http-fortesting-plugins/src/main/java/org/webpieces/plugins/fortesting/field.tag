<div id="${field.id}$_div" class="control-group ${field.errorClass}$">
        <label class="control-label">
           *{ This is for tooltip popup }*
           <a href="#" rel="tooltip" title="&{'', field.i18nKey+'.help'}&"><i class="icon-info-sign"></i></a>
           *{i18n fields are default text then key so read the label if it exists or lookup by key name}*
           &{optional('label'), field.i18nKey}&
        </label>
        <div class="controls">
            ${_body}$
            *{ I don't like the name 'help-block' as we use this for error messages but bootstrap calls it that
               ... someday, I may ask why on stackoverflow.  Users can override this whole template anyways }*
            <span id="${field.id}$_errorMsg" class="help-block">${field.error}$</span>
        </div>
</div>