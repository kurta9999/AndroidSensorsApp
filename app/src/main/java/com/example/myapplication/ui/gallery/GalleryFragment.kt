package com.example.myapplication.ui.gallery

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.TcpServerService
import com.example.myapplication.databinding.FragmentGalleryBinding
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberEndAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import java.util.Locale
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.scroll.rememberChartScrollSpec
import com.patrykandpatrick.vico.core.axis.Axis
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.scroll.AutoScrollCondition
import com.patrykandpatrick.vico.core.scroll.InitialScroll
import java.util.Date

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val chartEntryModelProducer = ChartEntryModelProducer(listOf(emptyList()))
    private val chartEntryModelProducer2 = ChartEntryModelProducer(listOf(emptyList()))
    private val chartEntryModelProducer3 = ChartEntryModelProducer(listOf(emptyList()))

    private var tempList: MutableList<Float> = mutableListOf()
    private var humList: MutableList<Float> = mutableListOf()
    private var co2List: MutableList<Float> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        TcpServerService.sensorLiveData.observe(viewLifecycleOwner) { data ->
            if(data.size > 8) {
                binding.textTemp.text = String.format(Locale.getDefault(), "Temp: %.1f", data[0])
                binding.textHum.text = String.format(Locale.getDefault(), "Hum: %.1f", data[1])
                binding.textCo2.text = String.format(Locale.getDefault(), "CO2: %.0f", data[2])
                binding.textIaqGas.text = String.format(Locale.getDefault(), "IAQ: %.0f", data[7])
                binding.textPressure.text = String.format(Locale.getDefault(), "P: %.0f", data[6])
                binding.textPm.text = String.format(Locale.getDefault(), "PM2.5: %.0f, PM10: %.0f", data[9], data[10])
                binding.textRGB.text = String.format(Locale.getDefault(), "R: %.0f, G: %.0f, B: %.0f", data[12], data[13], data[14])
                binding.textCct.text = String.format(Locale.getDefault(), "CCT: %.0f", data[15])
                binding.textLux.text = String.format(Locale.getDefault(), "Lux: %.0f", data[16])
                binding.textTime.text = String.format(Locale.getDefault(), "%s", SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.getDefault()).format(Date()))

                tempList.add(data[0]);
                humList.add(data[1]);
                co2List.add(data[2]);
                /*
                if(tempList.size > 100)
                    tempList.removeAt(0);
*/
                updateChart(chartEntryModelProducer, tempList.toList());
                updateChart(chartEntryModelProducer2, humList.toList());
                updateChart(chartEntryModelProducer3, co2List.toList());
            }
        }

        // Set up ComposeView for chart
        binding.chartView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                LineChartView(chartEntryModelProducer, "Temperature")
            }
        }
        binding.chartView2.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                LineChartView(chartEntryModelProducer2, "Humidity")
            }
        }
        binding.chartView3.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner))
            setContent {
                LineChartView(chartEntryModelProducer3, "CO2")
            }
        }
        return root
    }

    private fun updateChart(chartModel: ChartEntryModelProducer, data: List<Float>) {
        val entries: List<ChartEntry> = data.mapIndexed { index, value ->
            FloatEntry(index.toFloat(), value) // ✅ Correct way to create ChartEntry
        }

        chartModel.setEntries(listOf(entries)) // ✅ Ensure correct type (List<List<ChartEntry>>)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

@Composable
fun LineChartView(chartEntryModelProducer: ChartEntryModelProducer, title: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ✅ Chart Title
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Blue,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .padding(16.dp) // Add padding around the box
                .clip(RoundedCornerShape(12.dp)) // Rounded corners
                .border(2.dp, Color.Gray, RoundedCornerShape(12.dp)) // Gray border
                .background(Color.White) // Background color
                .padding(8.dp) // Inner padding
        ) {
            Chart(
                chart = lineChart(),
                chartModelProducer = chartEntryModelProducer,
                modifier = Modifier.fillMaxSize(),
                startAxis = rememberStartAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 100),
                    sizeConstraint = Axis.SizeConstraint.Auto(),
                ),
                bottomAxis = rememberBottomAxis(
                    guideline = null
                ),
                endAxis = rememberEndAxis(
                    itemPlacer = AxisItemPlacer.Vertical.default(maxItemCount = 100),
                    valueFormatter = { _, _ -> "" },
                    sizeConstraint = Axis.SizeConstraint.Auto()
                ),

                chartScrollSpec = rememberChartScrollSpec(
                    initialScroll = InitialScroll.End,
                    autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased,
                    isScrollEnabled = true,
                ),
            )
        }
    }
}