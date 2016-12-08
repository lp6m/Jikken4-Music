# Jikken4-Music
## 計算機科学実験　音楽情報処理

### lib/jfree-cheart-1.0.19_rebuild_with_javafx.jarについて.
`org.jfree.chart.fx.ChartViewer`はjfree-chart-1.0.19に含まれていないのでdistributionからソースをとってきて自分でビルドする.
その際にorg/jfree/chart/fx/ChartViewer.javaのgetUserAgentStylesheet()をprotectedからpublicに書き換える.
(http://www.jfree.org/forum/viewtopic.php?f=3&t=116963&start=15)
