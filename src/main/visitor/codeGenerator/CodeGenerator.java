package main.visitor.codeGenerator;

import main.ast.nodes.*;
import main.ast.nodes.declaration.*;
import main.ast.nodes.declaration.struct.*;
import main.ast.nodes.expression.*;
import main.ast.nodes.expression.operators.*;
import main.ast.nodes.expression.values.*;
import main.ast.nodes.expression.values.primitive.*;
import main.ast.nodes.statement.*;
import main.ast.types.*;
import main.ast.types.primitives.*;
import main.symbolTable.*;
import main.symbolTable.exceptions.*;
import main.symbolTable.items.FunctionSymbolTableItem;
import main.symbolTable.items.StructSymbolTableItem;
import main.visitor.Visitor;
import main.visitor.type.ExpressionTypeChecker;
import java.io.*;
import java.util.*;
public class  CodeGenerator extends Visitor<String> {

    ExpressionTypeChecker expressionTypeChecker = new ExpressionTypeChecker();
    private String outputPath;
    private FileWriter currentFile;

    private ArrayList<String> slots;
    private ArrayList<Integer> slotTypes;
    private boolean isMain;
    private boolean isFunctioncallStmt;
    private boolean isConstruct;

    private FileWriter mainFile;
    private FunctionDeclaration curFuncDec;
    private Set<String> visited;
    private int labelIndex;

    private FunctionSymbolTableItem currentFunction;


    private int getFreshLabel() {
        return labelIndex++;
    }

    public FunctionSymbolTableItem getFuncSymbolTableItem(String key) {
        try {
            return (FunctionSymbolTableItem) SymbolTable.root.getItem(key);
        } catch (ItemNotFoundException ignored) {
        }
        return null;
    }

    public String getArgTypeSymbol(Type t) {
        if (t instanceof IntType)
            return "Ljava/lang/Integer;";
        if (t instanceof BoolType)
            return "Ljava/lang/Boolean;";
        if (t instanceof ListType)
            return "LList;";
        if (t instanceof FptrType)
            return "LFptr;";
        if (t instanceof VoidType)
            return "V";
        return null;
    }

    //Defined by TA.
    private void copyFile(String toBeCopied, String toBePasted) {
        try {
            File readingFile = new File(toBeCopied);
            File writingFile = new File(toBePasted);
            InputStream readingFileStream = new FileInputStream(readingFile);
            OutputStream writingFileStream = new FileOutputStream(writingFile);
            byte[] buffer = new byte[1024];
            int readLength;
            while ((readLength = readingFileStream.read(buffer)) > 0)
                writingFileStream.write(buffer, 0, readLength);
            readingFileStream.close();
            writingFileStream.close();
        } catch (IOException e) {//unreachable
        }
    }

    //Defined by TA.
    private void prepareOutputFolder() {
        this.outputPath = "output/";
        String jasminPath = "utilities/jarFiles/jasmin.jar";
        String listClassPath = "utilities/codeGenerationUtilityClasses/List.j";
        String fptrClassPath = "utilities/codeGenerationUtilityClasses/Fptr.j";
        try {
            File directory = new File(this.outputPath);
            File[] files = directory.listFiles();
            if (files != null)
                for (File file : files)
                    file.delete();
            directory.mkdir();
        } catch (SecurityException e) {//unreachable

        }
        copyFile(jasminPath, this.outputPath + "jasmin.jar");
        copyFile(listClassPath, this.outputPath + "List.j");
        copyFile(fptrClassPath, this.outputPath + "Fptr.j");
    }

    //Defined by TA.
    private void createFile(String name) {
        try {
            String path = this.outputPath + name + ".j";
            File file = new File(path);
            file.createNewFile();
            this.currentFile = new FileWriter(path);
        } catch (IOException e) {//never reached
        }
    }

    //Defined by TA.
    private void addCommand(String command) {
        try {
            command = String.join("\n\t\t", command.split("\n"));
            if (command.startsWith("Label_"))
                this.currentFile.write("\t" + command + "\n");
            else if (command.startsWith("."))
                this.currentFile.write(command + "\n");
            else
                this.currentFile.write("\t\t" + command + "\n");
            this.currentFile.flush();
        } catch (IOException e) {//unreachable
        }
    }


    //Defined by TA.
    private void addStaticMainMethod() {
        addCommand(".method public static main([Ljava/lang/String;)V");
        addCommand(".limit stack 128");
        addCommand(".limit locals 128");
        addCommand("new Main");
        addCommand("invokespecial Main/<init>()V");
        addCommand("return");
        addCommand(".end method");
    }


    private int slotOf(String identifier) {
        if (identifier == "") {
            return slots.size();
        }
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i) == identifier) {
                return i;
            }
        }
        return -1;
    }

    private String slotType(String identifier) {
        return slotTypes.get(slotOf(identifier)) == 1 ? "i" : "a";
    }



    //Defined by TA.
    @Override
    public String visit(Program program) {
        prepareOutputFolder();

        for(StructDeclaration structDeclaration : program.getStructs()){
            structDeclaration.accept(this);
        }

        createFile("Main");

        program.getMain().accept(this);

        for (FunctionDeclaration functionDeclaration: program.getFunctions()){
            functionDeclaration.accept(this);
        }
        return null;
    }

    @Override
    public String visit(StructDeclaration structDeclaration) {
        try{
            String structKey = StructSymbolTableItem.START_KEY + structDeclaration.getStructName().getName();
            StructSymbolTableItem structSymbolTableItem = (StructSymbolTableItem)SymbolTable.root.getItem(structKey);
            SymbolTable.push(structSymbolTableItem.getStructSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }
        createFile(structDeclaration.getStructName().getName());

        //todo

        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(FunctionDeclaration functionDeclaration) {
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + functionDeclaration.getFunctionName().getName();
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }

        //todo

        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(MainDeclaration mainDeclaration) {
        try{
            String functionKey = FunctionSymbolTableItem.START_KEY + "main";
            FunctionSymbolTableItem functionSymbolTableItem = (FunctionSymbolTableItem)SymbolTable.root.getItem(functionKey);
            SymbolTable.push(functionSymbolTableItem.getFunctionSymbolTable());
        }catch (ItemNotFoundException e){//unreachable
        }

        //todo

        SymbolTable.pop();
        return null;
    }

    @Override
    public String visit(VariableDeclaration variableDeclaration) {
        //todo
        return null;
    }

    //Defined by TA.
    @Override
    public String visit(SetGetVarDeclaration setGetVarDeclaration) {
        return null;
    }

    @Override
    public String visit(AssignmentStmt assignmentStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(BlockStmt blockStmt) {
        StringBuilder new_command = new StringBuilder();
        for (Statement stmt : blockStmt.getStatements())
            new_command.append(stmt.accept(this)).append('\n');
        addCommand(new_command.toString());
        return null;
    }

    public String dummyInstruction()
    {
        return """
                iconst_0
                pop
                """;
    }

    @Override
    public String visit(ConditionalStmt conditionalStmt) {
        String elseLabel = "Label" + getFreshLabel();
        String afterLabel = "Label" + getFreshLabel();
        String new_command = "";
        new_command += conditionalStmt.getCondition().accept(this);
        new_command += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
        new_command += "ifeq " + elseLabel + "\n";
        new_command += conditionalStmt.getThenBody().accept(this);
        new_command += "goto " + afterLabel + "\n";
        new_command += elseLabel + ":\n";
        new_command += dummyInstruction();
        if (conditionalStmt.getElseBody() != null)
            new_command += conditionalStmt.getElseBody().accept(this);
        new_command += afterLabel + ":\n";
        new_command += dummyInstruction();
        addCommand(new_command);

        return null;
    }

    @Override
    public String visit(FunctionCallStmt functionCallStmt) {
        String new_command = "";
        isFunctioncallStmt = true;
        new_command += functionCallStmt.getFunctionCall().accept(this);

        Type t = functionCallStmt.getFunctionCall().accept(expressionTypeChecker);
        if (!(t instanceof VoidType))
            new_command += "pop\n";

        isFunctioncallStmt = false;
        addCommand(new_command);

        return null;
    }

    //Defined by TA.
    @Override
    public String visit(DisplayStmt displayStmt) {
        addCommand("getstatic java/lang/System/out Ljava/io/PrintStream;");
        Type argType = displayStmt.getArg().accept(expressionTypeChecker);
        String commandsOfArg = displayStmt.getArg().accept(this);

        addCommand(commandsOfArg);
        if (argType instanceof IntType)
            addCommand("invokevirtual java/io/PrintStream/println(I)V");
        if (argType instanceof BoolType)
            addCommand("invokevirtual java/io/PrintStream/println(Z)V");

        return null;
    }

    @Override
    public String visit(ReturnStmt returnStmt) {
        String new_command = "";
        new_command += returnStmt.getReturnedExpr().accept(this);
        Type returnType = returnStmt.getReturnedExpr().accept(expressionTypeChecker);
        if (returnType instanceof VoidType)
            new_command += "return\n";
        else
            new_command += "areturn\n";
        addCommand(new_command);
        return null;
    }

    @Override
    public String visit(LoopStmt loopStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(VarDecStmt varDecStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(ListAppendStmt listAppendStmt) {
        //todo
        return null;
    }

    @Override
    public String visit(ListSizeStmt listSizeStmt) {
        //todo
        return null;
    }

    public String getOperationCommand(BinaryOperator binOp) {
        if (binOp.equals(BinaryOperator.add))
            return "iadd\n";
        if (binOp.equals(BinaryOperator.sub))
            return "isub\n";
        if (binOp.equals(BinaryOperator.mult))
            return "imul\n";
        if (binOp.equals(BinaryOperator.div))
            return "idiv\n";
        return null;
    }

    @Override
    public String visit(BinaryExpression binaryExpression) {

        String new_command = "";

        String commandLeft = binaryExpression.getFirstOperand().accept(this);
        String commandRight = binaryExpression.getSecondOperand().accept(this);


        Type tl = binaryExpression.getFirstOperand().accept(expressionTypeChecker);
        Type tr = binaryExpression.getSecondOperand().accept(expressionTypeChecker);

        BinaryOperator operator = binaryExpression.getBinaryOperator();

        if (operator.equals(BinaryOperator.add) ||
                operator.equals(BinaryOperator.sub) ||
                operator.equals(BinaryOperator.mult) ||
                operator.equals(BinaryOperator.div)) {

            new_command += commandLeft;
            new_command += "invokevirtual java/lang/Integer/intValue()I\n";

            new_command += commandRight;
            new_command += "invokevirtual java/lang/Integer/intValue()I\n";

            new_command += getOperationCommand(operator);
            new_command += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
        }

        if (operator.equals(BinaryOperator.and) || operator.equals(BinaryOperator.or)) {

            String elseLabel = "Label" + getFreshLabel();
            String afterLabel = "Label" + getFreshLabel();

            new_command += commandLeft;
            new_command += "invokevirtual java/lang/Boolean/booleanValue()Z\n";

            if (operator.equals(BinaryOperator.and))
                new_command += "ifeq " + elseLabel + "\n";
            else
                new_command += "ifne " + elseLabel + "\n";

            new_command += commandRight;
            new_command += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
            new_command += "goto " + afterLabel + "\n";
            new_command += elseLabel + ":\n";

            new_command += "iconst_" + (operator.equals(BinaryOperator.or) ? "1" : "0") + "\n";

            new_command += afterLabel + ":\n";
            new_command += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
        }

        if (operator.equals(BinaryOperator.lt) || operator.equals(BinaryOperator.gt))
        {
            String elseLabel = "Label" + getFreshLabel();
            String afterLabel = "Label" + getFreshLabel();
            String ifCommand = "";


            new_command += commandLeft;
            new_command += "invokevirtual java/lang/Integer/intValue()I\n";
            new_command += commandRight;
            new_command += "invokevirtual java/lang/Integer/intValue()I\n";

            if (operator.equals(BinaryOperator.lt))
                ifCommand = "if_icmpge";
            else
                ifCommand = "if_icmple";


            new_command += ifCommand + " " + elseLabel + "\n";
            new_command += "iconst_1\n";
            new_command += "goto " + afterLabel + "\n";
            new_command += elseLabel + ":\n";
            new_command += "iconst_0\n";
            new_command += afterLabel + ":\n";
            new_command += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
        }

        if (operator.equals(BinaryOperator.eq)) {
            String elseLabel = "Label" + getFreshLabel();
            String afterLabel = "Label" + getFreshLabel();
            String ifCommand = "";

            if (tl instanceof IntType)
            {
                new_command += commandLeft;
                new_command += "invokevirtual java/lang/Integer/intValue()I\n";
                new_command += commandRight;
                new_command += "invokevirtual java/lang/Integer/intValue()I\n";

                ifCommand = "if_icmpne";

            }

            if (tl instanceof BoolType)
            {
                new_command += commandLeft;
                new_command += "invokevirtual java/lang/Boolean/booleanValue()Z\n";
                new_command += commandRight;
                new_command += "invokevirtual java/lang/Boolean/booleanValue()Z\n";

                ifCommand = "if_icmpne";

            }

            if (tl instanceof ListType || tl instanceof FptrType)
            {
                new_command += commandLeft;
                new_command += commandRight;

                ifCommand = "if_acmpne";

            }

            new_command += ifCommand + " " + elseLabel + "\n";
            new_command += "iconst_1\n";
            new_command += "goto " + afterLabel + "\n";
            new_command += elseLabel + ":\n";
            new_command += "iconst_0\n";
            new_command += afterLabel + ":\n";
            new_command += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
        }

        addCommand(new_command);

        return null;
    }

    //Defined by TA.
    @Override
    public String visit(UnaryExpression unaryExpression){
        return null;
    }

    @Override
    public String visit(StructAccess structAccess){
        //todo
        return null;
    }

    @Override
    public String visit(Identifier identifier){
        FunctionSymbolTableItem fsti = getFuncSymbolTableItem("Function_" + identifier.getName());
        String new_command = "";
        if (fsti == null) { //Not a function name
            int slot = slotOf(identifier.getName());
            new_command = "aload " + slot + "\n";
        }
        else { //is a function name
            new_command += "new Fptr\n" +
                    "dup\n" +
                    (isMain ? "aload_1\n" : "aload_0\n") +
                    "ldc \"" + identifier.getName() + "\"\n" +
                    "invokespecial Fptr/<init>(Ljava/lang/Object;Ljava/lang/String;)V\n";
        }
        addCommand(new_command);

        return null;
    }

    @Override
    public String visit(ListAccessByIndex listAccessByIndex){
        String commandList = listAccessByIndex.getInstance().accept(this);
        String commandIndex = listAccessByIndex.getIndex().accept(this);

        String new_command = "";
        new_command += commandList;
        new_command += commandIndex;
        new_command += "invokevirtual java/lang/Integer/intValue()I\n";
        new_command += "invokevirtual List/getElement(I)Ljava/lang/Object;\n";
        new_command += "checkcast java/lang/Integer\n";
        addCommand(new_command);

        return null;
    }

    public String getTypeCastString(Type type) {
        if (type instanceof VoidType) {
            return "\n";
        }

        if (type instanceof IntType) {
            return "checkcast java/lang/Integer\n";
        }

        if (type instanceof BoolType) {
            return "checkcast java/lang/Boolean\n";
        }

        if (type instanceof ListType) {
            return "checkcast List\n";
        }

        if (type instanceof FptrType) {
            return "checkcast Fptr\n";
        }

        return "\n";
    }

    @Override
    public String visit(FunctionCall functionCall){

        ArrayList<String> argByteCodes = new ArrayList<>();

        for (Expression expression : functionCall.getArgs()) {
            String bc = expression.accept(this);
            Type type = expression.accept(expressionTypeChecker);
            if (type instanceof ListType) {
                bc = "new List\n" +
                        "dup\n" +
                        bc +
                        "invokespecial List/<init>(LList;)V\n";
            }
            argByteCodes.add(bc);
        }

        String new_command = "";
        new_command += functionCall.getInstance().accept(this);

        new_command += """
                new java/util/ArrayList
                dup
                invokespecial java/util/ArrayList/<init>()V
                """;

        for (String bc: argByteCodes) {
            new_command += "dup\n";
            new_command += bc;
            new_command += "invokevirtual java/util/ArrayList/add(Ljava/lang/Object;)Z\n";
            new_command += "pop\n";
        }

        new_command += "invokevirtual Fptr/invoke(Ljava/util/ArrayList;)Ljava/lang/Object;\n";


        /*After function call is executed, stack top
            is an object of type Object; an appropriate type-
            casting must be performed to avoid errors in future
            uses of this value
         */
        Type returnType = functionCall.accept(expressionTypeChecker);
        new_command += getTypeCastString(returnType);


        addCommand(new_command);

        return null;
    }

    @Override
    public String visit(ListSize listSize){
        String commandList = listSize.getArg().accept(this);
        String new_command = "";
        new_command += commandList;
        new_command += "invokevirtual List/getSize()I\n";
        new_command += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
        addCommand(new_command);

        return null;
    }

    @Override
    public String visit(ListAppend listAppend) {
        //todo
        return null;
    }

    @Override
    public String visit(IntValue intValue) {
        String new_command = "";
        new_command += "ldc " + String.valueOf(intValue.getConstant()) + "\n";
        new_command += "invokestatic java/lang/Integer/valueOf(I)Ljava/lang/Integer;\n";
        addCommand(new_command);
        return null;
    }

    @Override
    public String visit(BoolValue boolValue) {
        String new_command = "";
        if (boolValue.getConstant())
            new_command += "ldc 1\n";
        else
            new_command += "ldc 0\n";
        new_command += "invokestatic java/lang/Boolean/valueOf(Z)Ljava/lang/Boolean;\n";
        addCommand(new_command);

        return null;
    }

    //Defined by TA.
    @Override
    public String visit(ExprInPar exprInPar) {
        return exprInPar.getInputs().get(0).accept(this);
    }
}
