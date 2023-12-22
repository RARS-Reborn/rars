package rars;

import rars.venus.VenusUI;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ReflectionDumper {

    public static class Node {
        String name;
        String type;

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        List<Node> children;

        public Node(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public Node(String name, String type, List<Node> children) {
            this.name = name;
            this.type = type;
            this.children = children;
        }
        String printNode(int depth) {
            StringBuilder sb = new StringBuilder();
            sb.append("  ".repeat(depth));
            sb.append(name);
            sb.append(":");
            sb.append(type);
            sb.append("\n");
            for (Node child : (children == null? new ArrayList<Node>() : children)) {
                sb.append(child.printNode(depth + 1));
            }
            return sb.toString();
        }

        String printNode() {
            return printNode(0);
        }

        @Override
        public String toString() {
            return "Node{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }

    static HashSet<Object> visited = new HashSet<>();

    public static void dumpUi(VenusUI ui) {
        try {
            var fld = ui.getClass().getDeclaredField("mainUI");
            var node = new Node(fld.getName(), fld.getType().getTypeName());
            node.setChildren(dump(ui));
            System.out.println(node.printNode());
            Files.writeString(Path.of("all-node-dump.txt"), node.printNode());
        } catch (NoSuchFieldException | IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
        System.exit(0);
    }

    public static List<Node> dump(Object obj) throws IllegalAccessException {
        if (obj == null) {
            return null;
        }
        var cls = obj.getClass();
        var type = cls.getTypeName();
        if (!type.startsWith("rars") || visited.contains(obj)) {
            return null;
        }
        visited.add(obj);
        List<Node> children = new ArrayList<>();

        var fields = cls.getDeclaredFields();
        for (var field : fields) {
            field.setAccessible(true);
            var fieldValue = field.get(obj);
            if (visited.contains(fieldValue)) {
                continue;
            }

            var fieldName = field.getName();
            var fieldType = field.getType().getTypeName();
            var node = new Node(fieldName, fieldType);
            List<Node> nodeChildren = dump(fieldValue);
            node.setChildren(nodeChildren);
            children.add(node);
        }

        return children;
    }
}
