package hcmus.java.server;

import java.util.List;

public class Group {
    public static int currentGroupID = 1;

    public int id;
    public String name;
    public List<String> users;

    public Group(String name, List<String> users) {
        this.id = currentGroupID++;
        this.name = name;
        this.users = users;
    }

    public static Group findGroup(List<Group> groupList, int id) {
        for (Group group : groupList)
            if (group.id == id)
                return group;
        return null;
    }
}
