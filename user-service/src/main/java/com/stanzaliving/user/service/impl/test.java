package com.stanzaliving.user.service.impl;

import com.stanzaliving.core.base.enums.Department;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class test {
    public void make_list(List<String> roleUuids, List<String> accessLevels, List<String> departments, String userUuids, List<String> mmIds) throws IOException {
        File myObj = new File("listing.txt");
        FileWriter myWriter = new FileWriter("listing.txt");
        for (int j = 0; j < mmIds.size(); j++) {
            for (int i = 0; i <roleUuids.size();i++){
                List<String> newRow = new ArrayList<>();
                newRow.add(roleUuids.get(i));
                newRow.add(accessLevels.get(i));
                newRow.add(departments.get(i));
                newRow.add(userUuids);
                newRow.add(mmIds.get(j));
                myWriter.write(newRow.toString());
                myWriter.append("\n");
            }
        }
        myWriter.close();
    }
}
