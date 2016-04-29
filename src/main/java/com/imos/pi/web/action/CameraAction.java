/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.pi.web.action;

import com.imos.pi.web.utils.MailProperty;
import javax.faces.bean.ManagedBean;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Alok
 */
@ManagedBean(name = "cameraAction")
public class CameraAction {
    
    @Setter @Getter
    private MailProperty mailProperty;
    
    @Setter @Getter
    private boolean enable;
    
    public void save() {
        
    }
}
