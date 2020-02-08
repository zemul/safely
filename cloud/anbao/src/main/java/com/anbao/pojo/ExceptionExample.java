package com.anbao.pojo;

import java.util.ArrayList;
import java.util.List;

public class ExceptionExample {
    protected String orderByClause;

    protected boolean distinct;

    protected List<Criteria> oredCriteria;

    public ExceptionExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andEidIsNull() {
            addCriterion("eid is null");
            return (Criteria) this;
        }

        public Criteria andEidIsNotNull() {
            addCriterion("eid is not null");
            return (Criteria) this;
        }

        public Criteria andEidEqualTo(String value) {
            addCriterion("eid =", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidNotEqualTo(String value) {
            addCriterion("eid <>", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidGreaterThan(String value) {
            addCriterion("eid >", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidGreaterThanOrEqualTo(String value) {
            addCriterion("eid >=", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidLessThan(String value) {
            addCriterion("eid <", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidLessThanOrEqualTo(String value) {
            addCriterion("eid <=", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidLike(String value) {
            addCriterion("eid like", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidNotLike(String value) {
            addCriterion("eid not like", value, "eid");
            return (Criteria) this;
        }

        public Criteria andEidIn(List<String> values) {
            addCriterion("eid in", values, "eid");
            return (Criteria) this;
        }

        public Criteria andEidNotIn(List<String> values) {
            addCriterion("eid not in", values, "eid");
            return (Criteria) this;
        }

        public Criteria andEidBetween(String value1, String value2) {
            addCriterion("eid between", value1, value2, "eid");
            return (Criteria) this;
        }

        public Criteria andEidNotBetween(String value1, String value2) {
            addCriterion("eid not between", value1, value2, "eid");
            return (Criteria) this;
        }

        public Criteria andInittimeIsNull() {
            addCriterion("inittime is null");
            return (Criteria) this;
        }

        public Criteria andInittimeIsNotNull() {
            addCriterion("inittime is not null");
            return (Criteria) this;
        }

        public Criteria andInittimeEqualTo(String value) {
            addCriterion("inittime =", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeNotEqualTo(String value) {
            addCriterion("inittime <>", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeGreaterThan(String value) {
            addCriterion("inittime >", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeGreaterThanOrEqualTo(String value) {
            addCriterion("inittime >=", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeLessThan(String value) {
            addCriterion("inittime <", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeLessThanOrEqualTo(String value) {
            addCriterion("inittime <=", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeLike(String value) {
            addCriterion("inittime like", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeNotLike(String value) {
            addCriterion("inittime not like", value, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeIn(List<String> values) {
            addCriterion("inittime in", values, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeNotIn(List<String> values) {
            addCriterion("inittime not in", values, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeBetween(String value1, String value2) {
            addCriterion("inittime between", value1, value2, "inittime");
            return (Criteria) this;
        }

        public Criteria andInittimeNotBetween(String value1, String value2) {
            addCriterion("inittime not between", value1, value2, "inittime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeIsNull() {
            addCriterion("continuetime is null");
            return (Criteria) this;
        }

        public Criteria andContinuetimeIsNotNull() {
            addCriterion("continuetime is not null");
            return (Criteria) this;
        }

        public Criteria andContinuetimeEqualTo(String value) {
            addCriterion("continuetime =", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeNotEqualTo(String value) {
            addCriterion("continuetime <>", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeGreaterThan(String value) {
            addCriterion("continuetime >", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeGreaterThanOrEqualTo(String value) {
            addCriterion("continuetime >=", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeLessThan(String value) {
            addCriterion("continuetime <", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeLessThanOrEqualTo(String value) {
            addCriterion("continuetime <=", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeLike(String value) {
            addCriterion("continuetime like", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeNotLike(String value) {
            addCriterion("continuetime not like", value, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeIn(List<String> values) {
            addCriterion("continuetime in", values, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeNotIn(List<String> values) {
            addCriterion("continuetime not in", values, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeBetween(String value1, String value2) {
            addCriterion("continuetime between", value1, value2, "continuetime");
            return (Criteria) this;
        }

        public Criteria andContinuetimeNotBetween(String value1, String value2) {
            addCriterion("continuetime not between", value1, value2, "continuetime");
            return (Criteria) this;
        }

        public Criteria andVideourlIsNull() {
            addCriterion("videourl is null");
            return (Criteria) this;
        }

        public Criteria andVideourlIsNotNull() {
            addCriterion("videourl is not null");
            return (Criteria) this;
        }

        public Criteria andVideourlEqualTo(String value) {
            addCriterion("videourl =", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlNotEqualTo(String value) {
            addCriterion("videourl <>", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlGreaterThan(String value) {
            addCriterion("videourl >", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlGreaterThanOrEqualTo(String value) {
            addCriterion("videourl >=", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlLessThan(String value) {
            addCriterion("videourl <", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlLessThanOrEqualTo(String value) {
            addCriterion("videourl <=", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlLike(String value) {
            addCriterion("videourl like", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlNotLike(String value) {
            addCriterion("videourl not like", value, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlIn(List<String> values) {
            addCriterion("videourl in", values, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlNotIn(List<String> values) {
            addCriterion("videourl not in", values, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlBetween(String value1, String value2) {
            addCriterion("videourl between", value1, value2, "videourl");
            return (Criteria) this;
        }

        public Criteria andVideourlNotBetween(String value1, String value2) {
            addCriterion("videourl not between", value1, value2, "videourl");
            return (Criteria) this;
        }

        public Criteria andMacIsNull() {
            addCriterion("mac is null");
            return (Criteria) this;
        }

        public Criteria andMacIsNotNull() {
            addCriterion("mac is not null");
            return (Criteria) this;
        }

        public Criteria andMacEqualTo(String value) {
            addCriterion("mac =", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacNotEqualTo(String value) {
            addCriterion("mac <>", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacGreaterThan(String value) {
            addCriterion("mac >", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacGreaterThanOrEqualTo(String value) {
            addCriterion("mac >=", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacLessThan(String value) {
            addCriterion("mac <", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacLessThanOrEqualTo(String value) {
            addCriterion("mac <=", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacLike(String value) {
            addCriterion("mac like", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacNotLike(String value) {
            addCriterion("mac not like", value, "mac");
            return (Criteria) this;
        }

        public Criteria andMacIn(List<String> values) {
            addCriterion("mac in", values, "mac");
            return (Criteria) this;
        }

        public Criteria andMacNotIn(List<String> values) {
            addCriterion("mac not in", values, "mac");
            return (Criteria) this;
        }

        public Criteria andMacBetween(String value1, String value2) {
            addCriterion("mac between", value1, value2, "mac");
            return (Criteria) this;
        }

        public Criteria andMacNotBetween(String value1, String value2) {
            addCriterion("mac not between", value1, value2, "mac");
            return (Criteria) this;
        }
    }

    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}