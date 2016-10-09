package utils;

import bean.Tuple;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/***
 * 计算器
 * 先词法分析,然后语法分析
 * 暂时还不支持括号,有空再做
 */
public class Calculator {

    /** 文法
    expr:term|(expr+|-expr)
    term:number|(term *|/term)
    number:double
*/



    public double calculate(String text) {
        Preconditions.checkArgument(StringUtils.isNotEmpty(text), "真是可笑,你居然输入了一个空的字符串");
        List<TokenItem> tokens = getTokens(text);
        Tuple<Double, Integer> doubleIntegerTuple = calculateExpr(tokens, 0);
        return doubleIntegerTuple.getT();
    }

    private TokenItem getToken(List<TokenItem> tokenItems, int nowIndex) {
        Preconditions.checkArgument(nowIndex >= 0 && nowIndex < tokenItems.size());
        return tokenItems.get(nowIndex);
    }

    private Tuple<Double, Integer> calculateExpr(List<TokenItem> tokenItems, int nowIndex) {
        TokenItem token = getToken(tokenItems, nowIndex++);
        Preconditions.checkNotNull(token);
        Preconditions.checkState(token.getType().getCode() == CalculatorTokenType.enumTokenType_number.code);
        double tokenNum = Double.parseDouble(token.content);
        Tuple<Double, Integer> result = new Tuple<>(tokenNum, nowIndex);
        while (nowIndex < tokenItems.size()) {
            TokenItem token2 = getToken(tokenItems, nowIndex++);
            if (token2.getType().getCode() == CalculatorTokenType.enumTokenType_add.getCode()) {
                Tuple<Double, Integer> reResult = calculateTerm(tokenItems, nowIndex);
                tokenNum += reResult.getT();
                nowIndex = reResult.getK();
            } else if (token2.getType().getCode() == CalculatorTokenType.enumTokenType_sub.getCode()) {
                Tuple<Double, Integer> reResult = calculateTerm(tokenItems, nowIndex);
                tokenNum -= reResult.getT();
                nowIndex = reResult.getK();
            } else {
                Preconditions.checkState(token2.getType().code != CalculatorTokenType.enumTokenType_none.code, "表达式包含未知字符");
                Tuple<Double, Integer> reResult = calculateTerm(tokenItems, nowIndex - 2);
                tokenNum = reResult.getT();
                nowIndex = reResult.getK();
            }
        }
        result.setK(nowIndex);
        result.setT(tokenNum);

        return result;
    }

    private Tuple<Double, Integer> calculateTerm(List<TokenItem> tokenItems, int nowIndex) {
        //检查
        TokenItem token = getToken(tokenItems, nowIndex++);
        Preconditions.checkNotNull(token);
        Preconditions.checkState(token.getType().getCode() == CalculatorTokenType.enumTokenType_number.code);
        double tokenNum = Double.parseDouble(token.content);
        Tuple<Double, Integer> result = new Tuple<>(tokenNum, nowIndex);
        while (nowIndex < tokenItems.size() ) {
            TokenItem token2 = getToken(tokenItems, nowIndex++);
            if (token2.getType().getCode() != CalculatorTokenType.enumTokenType_mul.getCode()
                    && token2.getType().getCode() != CalculatorTokenType.enumTokenType_div.getCode()) {
                Preconditions.checkState(token2.getType().code != CalculatorTokenType.enumTokenType_none.code, "表达式包含未知字符");
                result.setT(tokenNum);
                result.setK(--nowIndex);
                return result;
            } else {
                if (token2.getType().getCode() == CalculatorTokenType.enumTokenType_div.getCode()) {
                    token2 = getToken(tokenItems, nowIndex++);
                    double token2Num = Double.parseDouble(token2.content);
                    Preconditions.checkState(token2Num - 0 > 0.00000001, "除数不能为0");
                    tokenNum /= token2Num;
                } else {
                    token2 = getToken(tokenItems, nowIndex++);
                    double token2Num = Double.parseDouble(token2.content);
                    tokenNum *= token2Num;
                }
            }
        }
        result.setT(tokenNum);
        result.setK(nowIndex);
        return result;
    }



    private List<TokenItem> getTokens(String text) {
        text += "~";
        char[] textChars = text.toCharArray();
        char last = '#';
        StringBuilder sb = new StringBuilder();
        List<TokenItem> resultLists = Lists.newArrayList();
        for (char c : textChars) {
            if (sb.length() == 0) {
                sb.append(c);
                last = c;
            } else {
                if (isNumber(last)) {
                    if (isNumberNext(c)) {
                        sb.append(c);
                        last = c;
                    } else {
                        TokenItem item = new TokenItem(sb.toString(),
                                CalculatorTokenType.enumTokenType_number);
                        resultLists.add(item);
                        sb = new StringBuilder();
                        sb.append(c);
                        last = c;
                    }
                } else if (last == '.') {
                    if (isNumber(c)) {
                        sb.append(c);
                        last = c;
                    } else {
                        throw new RuntimeException("token 不能以小数点结尾");
                    }
                } else if (isOperator(last)) {
                    CalculatorTokenType tokenType = null;
                    tokenType = whatOperator(last);
                    TokenItem item = new TokenItem(sb.toString(),
                            tokenType);
                    resultLists.add(item);
                    sb = new StringBuilder();
                    sb.append(c);
                    last = c;
                } else if (c == '~') {//结束符处理
                    if (isNumber(last)) {
                        TokenItem item = new TokenItem(sb.toString(),
                                CalculatorTokenType.enumTokenType_number);
                        resultLists.add(item);
                    } else if (isOperator(last)) {
                        TokenItem item = new TokenItem(sb.toString(),
                                whatOperator(c));
                        resultLists.add(item);
                    }
                }
            }

        }
        return resultLists;
    }

    private CalculatorTokenType whatOperator(char c) {
        if (c == '+') {
            return CalculatorTokenType.enumTokenType_add;
        }
        if (c == '-') {
            return CalculatorTokenType.enumTokenType_sub;
        }
        if (c == '*') {
            return CalculatorTokenType.enumTokenType_mul;
        }
        if (c == '/') {
            return CalculatorTokenType.enumTokenType_div;
        }
        throw new RuntimeException("非运算符");
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }


    private boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isNumberNext(char c) {
        return c == '.' || c >= '0' && c <= '9';
    }

    public static enum CalculatorTokenType {

        enumTokenType_none(0),
        enumTokenType_number(1),  // 数字
        // enumTokenType_float(2),// 浮点数
        enumTokenType_add(3),// 加号
        enumTokenType_sub(4),// 减号
        enumTokenType_mul(5),// 乘号
        enumTokenType_div(6),// 除号
        ;

        private int code;

        CalculatorTokenType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }


    public static class TokenItem {
        private String content;
        private CalculatorTokenType type;

        public TokenItem(String content, CalculatorTokenType type) {
            this.content = content;
            this.type = type;
        }

        public TokenItem() {
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public CalculatorTokenType getType() {
            return type;
        }

        public void setType(CalculatorTokenType type) {
            this.type = type;
        }
    }
}
