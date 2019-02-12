package com.mewna.catnip;

public class StupidTest {
    
    public static void main(String[] args) {
        Catnip.catnipAsync("NTM1MTE0MDk2NzMxNzUwNDAx.D0RbJg.ttJHF4YjMWRcn-aWECiuDvS--eo").thenAccept(cat -> {
//            cat.rest().channel().sendMessage("507627405163364363", "HUIUIUI");
            cat.rest().guild().addGuildMemberRole("507517900127469568", "416902379598774273", "525781595379073025", "Hiii").thenAccept(req -> System.out.println("SUCCESS"));
        });
    }
}
