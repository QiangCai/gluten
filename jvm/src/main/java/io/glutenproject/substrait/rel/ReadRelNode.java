package io.glutenproject.substrait.rel;

import io.glutenproject.substrait.SubstraitContext;
import io.glutenproject.substrait.expression.ExpressionNode;
import io.glutenproject.substrait.type.TypeNode;
import io.substrait.proto.*;

import java.io.Serializable;
import java.util.ArrayList;

public class ReadRelNode implements RelNode, Serializable {
    private final ArrayList<TypeNode> types = new ArrayList<>();
    private final ArrayList<String> names = new ArrayList<>();
    private final ExpressionNode filterNode;
    private final SubstraitContext context;

    ReadRelNode(ArrayList<TypeNode> types, ArrayList<String> names,
                ExpressionNode filterNode, SubstraitContext context) {
        this.types.addAll(types);
        this.names.addAll(names);
        this.filterNode = filterNode;
        this.context = context;
    }

    ReadRelNode(ArrayList<TypeNode> types, ArrayList<String> names,
                SubstraitContext context) {
        this.types.addAll(types);
        this.names.addAll(names);
        this.filterNode = null;
        this.context = context;
    }

    @Override
    public Rel toProtobuf() {
        RelCommon.Builder relCommonBuilder = RelCommon.newBuilder();
        relCommonBuilder.setDirect(RelCommon.Direct.newBuilder());

        Type.Struct.Builder structBuilder = Type.Struct.newBuilder();
        for (TypeNode typeNode : types) {
            structBuilder.addTypes(typeNode.toProtobuf());
        }
        NamedStruct.Builder nStructBuilder = NamedStruct.newBuilder();
        nStructBuilder.setStruct(structBuilder.build());
        for (String name : names) {
            nStructBuilder.addNames(name);
        }
        ReadRel.Builder readBuilder = ReadRel.newBuilder();
        readBuilder.setCommon(relCommonBuilder.build());
        readBuilder.setBaseSchema(nStructBuilder.build());
        if (filterNode != null) {
            readBuilder.setFilter(filterNode.toProtobuf());
        }
        if (context.getLocalFilesNode() != null) {
            readBuilder.setLocalFiles(context.getLocalFilesNode().toProtobuf());
        } else if (context.getExtensionTableNode() != null) {
            readBuilder.setExtensionTable(context.getExtensionTableNode().toProtobuf());
        }
        Rel.Builder builder = Rel.newBuilder();
        builder.setRead(readBuilder.build());
        return builder.build();
    }
}
