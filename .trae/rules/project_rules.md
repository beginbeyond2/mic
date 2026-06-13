# 项目全局规则

## 图表绘制规则

**【重要】只要是图表的绘制，都优先使用Mermaid语法绘制。如果无法使用Mermaid语法绘制，再使用其他的方式绘制。**

此规则必须严格执行，适用于所有图表绘制场景，包括但不限于：
- 流程图
- 时序图
- 类图
- 状态图
- 实体关系图
- 甘特图
- 饼图
- 其他类型的图表

### Mermaid支持的图表类型

Mermaid支持以下图表类型，应优先使用：

1. **流程图 (Flowchart)** - 使用 `graph` 或 `flowchart`
2. **时序图 (Sequence Diagram)** - 使用 `sequenceDiagram`
3. **类图 (Class Diagram)** - 使用 `classDiagram`
4. **状态图 (State Diagram)** - 使用 `stateDiagram-v2`
5. **实体关系图 (ER Diagram)** - 使用 `erDiagram`
6. **甘特图 (Gantt Chart)** - 使用 `gantt`
7. **饼图 (Pie Chart)** - 使用 `pie`
8. **思维导图 (Mindmap)** - 使用 `mindmap`
9. **用户旅程图 (User Journey)** - 使用 `journey`
10. **Git图 (Git Graph)** - 使用 `gitGraph`

### 执行要求

- 在任何需要绘制图表的场景下，首先评估是否可以使用Mermaid语法
- 如果Mermaid支持该图表类型，必须使用Mermaid语法
- 只有在Mermaid确实无法满足需求时，才考虑其他绘图方式