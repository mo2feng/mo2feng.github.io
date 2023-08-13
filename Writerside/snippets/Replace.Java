class Replace {
    public static void main(String[] args) {
        String string = "abc123adb23456aa";
        System.out.println(string);//abc123adb23456aa

        //使用replace将a替换成H
        System.out.println(string.replace("a", "H"));//Hbc123Hdb23456HH
        //使用replaceFirst将第一个a替换成H
        System.out.println(string.replaceFirst("a", "H"));//Hbc123adb23456aa
        //使用replace将a替换成H
        System.out.println(string.replaceAll("a", "H"));//Hbc123Hdb23456HH

        //使用replaceFirst将第一个数字替换成H
        System.out.println(string.replaceFirst("\\d", "H"));//abcH23adb23456aa
        //使用replaceAll将所有数字替换成H
        System.out.println(string.replaceAll("\\d", "H"));//abcHHHadbHHHHHaa

    }
}