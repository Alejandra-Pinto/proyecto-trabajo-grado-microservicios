package com.example.users.patterns;

public class EvaluatorDecorator extends UserDecorator {

    public EvaluatorDecorator(IUserComponent decoratedUser) {
        super(decoratedUser);
    }

    public String evaluateThesis(String thesisTitle) {
        return getFullName() + " está evaluando el anteproyecto: " + thesisTitle;
    }

    @Override
    public String getRole() {
        return decoratedUser.getRole() + " (Evaluador)";
    }
}
