/*
 * eXist-db Open Source Native XML Database
 * Copyright (C) 2001 The eXist-db Authors
 *
 * info@exist-db.org
 * http://www.exist-db.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.exist.client.security;

import org.exist.client.DialogCompleteWithResponse;
import org.exist.client.DialogWithResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import org.exist.security.Account;
import org.exist.xmldb.UserManagementService;
import org.xmldb.api.base.XMLDBException;

/**
 *
 * @author <a href="mailto:adam.retter@googlemail.com">Adam Retter</a>
 */
public class FindUserForm extends javax.swing.JFrame implements DialogWithResponse<String> {
    private final UserManagementService userManagementService;
    private final List<DialogCompleteWithResponse<String>> dialogCompleteWithResponseCallbacks = new ArrayList<>();
    private final Set<String> allUsernames;
    private DefaultComboBoxModel<String> usernameModel;

    public FindUserForm(final UserManagementService userManagementService) throws XMLDBException {
        this.userManagementService = userManagementService;
        
        allUsernames = new HashSet<>();
        for(final Account account : userManagementService.getAccounts()) {
            allUsernames.add(account.getName());
        }
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblUsername = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        btnOk = new javax.swing.JButton();
        btnCancel = new javax.swing.JButton();
        cmbUsername = new javax.swing.JComboBox<>();
        AutoCompletion.enable(cmbUsername);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Find User...");

        lblUsername.setText("User name:");

        btnOk.setText("Ok");
        btnOk.setEnabled(false);
        btnOk.addActionListener(this::btnOkActionPerformed);

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(this::btnCancelActionPerformed);

        cmbUsername.setEditable(true);
        cmbUsername.setModel(getUsernameModel());
        cmbUsername.addActionListener(this::cmbUsernameActionPerformed);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblUsername)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cmbUsername, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSeparator1)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnCancel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOk)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUsername)
                    .addComponent(cmbUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOk)
                    .addComponent(btnCancel))
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnCancelActionPerformed

    private void cmbUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbUsernameActionPerformed
        final String currentUsername = (String)cmbUsername.getSelectedItem();
        final boolean isValid = isValidUsername(currentUsername);
        btnOk.setEnabled(isValid);
    }//GEN-LAST:event_cmbUsernameActionPerformed

    private void btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOkActionPerformed
        final String currentUsername = (String)cmbUsername.getSelectedItem();
        if(!isValidUsername(currentUsername)) {
            return;
        }
        
        for(final DialogCompleteWithResponse<String> callback : getDialogCompleteWithResponseCallbacks()) {
            callback.complete(currentUsername);
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btnOkActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JComboBox<String> cmbUsername;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel lblUsername;
    // End of variables declaration//GEN-END:variables


    private boolean isValidUsername(final String username) {
        return allUsernames.contains(username);
    }
    
    private ComboBoxModel<String> getUsernameModel() {
        if(usernameModel == null) {
            usernameModel = new DefaultComboBoxModel<>();
            usernameModel.addElement("");
            for(final String username : allUsernames) {
               usernameModel.addElement(username);
            }
        }
        
        return usernameModel;
    }
    
    private UserManagementService getUserManagementService() {
        return userManagementService;
    }
    
    @Override
    public void addDialogCompleteWithResponseCallback(final DialogCompleteWithResponse<String> dialogCompleteWithResponseCallback) {
        getDialogCompleteWithResponseCallbacks().add(dialogCompleteWithResponseCallback);
    }
    
    private List<DialogCompleteWithResponse<String>> getDialogCompleteWithResponseCallbacks() {
        return dialogCompleteWithResponseCallbacks;
    }
}
