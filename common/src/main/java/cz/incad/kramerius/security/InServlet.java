/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

import java.io.Reader;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.w3c.dom.Document;

import cz.incad.kramerius.FedoraAccess;

public class InServlet {
    
    /**
     *  persons | rightId
     *  ------------------
     *  pa.@gm. | 340
     *  
     *  
     *  rightId | params
     *  ----------------
     *   340    |  uuid,/movingwall.sh
     * =========================================
     * 
     * 
     *  ..movingwall.sh..
     *  ..
     *  var type = context.getFedoraAccess().getType();
     *  
     *  if (type is in chain 'monograph>*>page') { 
     *      return configuraion;
     *  } else return false;
     * 
     * @param args
     */
    
    public static void main(String[] args) {
        try {
            RightsManager factory = null;
            UserProvider provider = null;
            AbstractUser user = provider.get();
            Right right = factory.findRight("uuid", "readThumb", user);
            if ((right != null) &&  (right.getParam() == null)) {
                
            } else if ((right.getParam() != null) && (right.getParam().evalute())) {
                // povoleno 
                // pripad, kdy neni definovan parametr prava (bez parametru)
            } else {
                // zakazano
            }
        } catch (RightParamEvaluateContextException e) {
            //cannot execute param !
            e.printStackTrace();
        }
    }

    private static Right getRight(String string, String string2, String string3) {
        return null;
    }
}


