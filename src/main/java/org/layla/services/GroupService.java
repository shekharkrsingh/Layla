// services/GroupService.java
package org.layla.services;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class GroupService {
    private static final String ALLOWED_GROUPS_FILE = "allowed_groups.dat";
    private Set<Long> allowedGroupIds = new HashSet<>();

    public GroupService() {
        loadAllowedGroups();
    }

    public boolean isGroupAllowed(Long chatId) {
        return allowedGroupIds.contains(chatId);
    }

    public void addAllowedGroup(Long groupId) {
        allowedGroupIds.add(groupId);
        saveAllowedGroups();
    }

    public void removeAllowedGroup(Long groupId) {
        allowedGroupIds.remove(groupId);
    }


    public Set<Long> getAllowedGroups() {
        return new HashSet<>(allowedGroupIds);
    }

    private void loadAllowedGroups() {
        File file = new File(ALLOWED_GROUPS_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                allowedGroupIds = (Set<Long>) ois.readObject();
            } catch (Exception e) {
                System.err.println("Failed to load allowed groups: " + e.getMessage());
            }
        }
    }

    private void saveAllowedGroups() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ALLOWED_GROUPS_FILE))) {
            oos.writeObject(allowedGroupIds);
        } catch (IOException e) {
            System.err.println("Failed to save allowed groups: " + e.getMessage());
        }
    }
}