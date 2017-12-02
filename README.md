# JYacc #

1. 说明

	1. 描述文件及语法分析器的输入流编码均为`US-ASCII`（即通常所说的`ASCII`编码）

	2. 描述文件代码段中使用到的类用户将会自行解决import

	3. 输出的源代码文件为描述文件同目录下添加`.java`后缀

2. 描述文件结构

	> 描述文件共分四段，相互之间用`%%`进行分隔，分段内容可以为空但不能缺少分段，不允许注释的存在，除段落分隔符`%%`后至行末的内容及空行（空白字符包括空格、`\b`、`\t`、`\r`、`\n`等）会被忽略外，其余所有内容都将被解析，不在描述文件规范内的部分将造成解析失败（下方`//`开头的行仅为说明作用，不应出现在描述文件中）

		//第一段，代码段

		//用户代码，将被添加在输出文件的import部分之后，Yacc类之前，可选
		......

		%%

		//第二段，终结符列表

		//每行都将被解析为一个终结符，添加到终结符列表中，至少需要有一个终结符
		TERMINAL_SYMBOL_1
		TERMINAL_SYMBOL_2
		......

		%%

		//第三段，非终结符列表

		//每行都将被解析为一个非终结符，添加到非终结符列表中，至少需要有一个非终结符
		NONTERMINAL_SYMBOL_1
		NONTERMINAL_SYMBOL_2
		......

		%%

		//第四段，语法产生式列表

		//每一行都将被解析为一个产生式，不可跨行，至少应有一个产生式
		NONTERMINAL_SYMBOL1=SYMBOL1SYMBOL2SYMBOL3......
		NONTERMINAL_SYMBOL2=SYMBOL4SYMBOL5SYMBOL6......
		......

3. 词法解析器相关

	1. `Yacc`类对外开放两个构造方法`Yacc()`和`Yacc(Supplier<Token> tokenSupplier)`，前者将标准输入流中的每个ASCII字符作为一个Token，后者将使用参数中的Supplier获取Token

	2. 采用提供Supplier的构造方法中的Token类没有被实现，需要用户自行实现以下接口

			interface Token{
				String getText();
			}

	3. `Yacc`类中对外开放的解析方法为`void parse() throws IOException`，该方法将持续解析直到出错或解析完成

4. 其他说明

	使用IDE查看项目源代码时可能需要安装`lombok`插件
