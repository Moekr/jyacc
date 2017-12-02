class Application {
    public static void main(String[] args) throws IOException {
        new Yacc().parse();
    }
}

%%

i
+
*
(
)

%%

E
T
F

%%

E=E+T
E=T
T=T*F
T=F
F=(E)
F=i
