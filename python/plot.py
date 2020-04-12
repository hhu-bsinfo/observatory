from typing import final
from os import makedirs
import argparse
import pandas
import matplotlib.pyplot

COLORS: final = {'ibverbs': '#ff0000', 'jVerbs': '#00ff00', 'DiSNI': '#ff7f00',
                 'neutrino': '#0000ff', 'IPoIB': '#ff00ff', 'libvma': '#00ffff',
                 'JSOR': '#4d4d4d'}

MARKERS: final = ['+', 'x', 'o', 's', 'D', 'p', 'v', '^']
METRIC_TABLE: final = ['', 'K', 'M', 'G', 'T', 'P', 'E']
FONT: final = {'family': 'DejaVu Sans', 'weight': 'normal', 'size': 28}

FIGURE_SIZE: final = (16, 9)
LEGEND_POSITION: final = (0.5, 1.2)
LEGEND_COLUMNS: final = 3
LATENCY_YMIN: final = 0.91

matplotlib.rc('font', **FONT)


def format_byte_value(value):
    formatted_value = value
    counter = 0

    while formatted_value >= 1024 and formatted_value != 0 and counter < len(METRIC_TABLE) - 1:
        formatted_value /= 1024
        counter += 1

    if counter == 0:
        return '{}'.format(int(formatted_value))
    else:
        return '{} {}i'.format(int(formatted_value), METRIC_TABLE[counter])


def plot(data_frame, output_file, left_axis_column, left_axis_title, left_axis_linestyle='solid',
         left_logy=False, left_yerr=True, right_axis_column=None, right_axis_title=None, right_axis_linestyle='dashed',
         right_logy=False, right_yerr=True):

    left_values = data_frame.groupby(['Benchmark', 'Size'])[left_axis_column]
    left_medians = left_values.median().unstack().transpose()
    left_deviations = left_values.std().unstack().transpose()

    left_medians.axes[0].name = 'Size in Byte'
    left_deviations.axes[0].name = 'Size in Byte'

    colors = []
    for name in left_medians:
        colors.append(COLORS[name])

    x_ticks = left_medians.axes[0][::2]
    x_labels = list(map(format_byte_value, x_ticks))

    ax = left_medians.plot(kind='line', linestyle=left_axis_linestyle, elinewidth=2, capthick=2,
                           capsize=5, legend=False, mark_right=False, grid=True, logx=True, logy=left_logy,
                           xticks=x_ticks, xlim=(x_ticks[0], x_ticks[len(x_ticks) - 1]),
                           figsize=FIGURE_SIZE, color=colors, yerr=(left_deviations if left_yerr else False))

    ax.set_ylabel(left_axis_title)

    if left_axis_title.startswith('Latency'):
        ax.set_ylim(LATENCY_YMIN)

    ax.xaxis.set_tick_params(which='minor', bottom=False)
    ax.set_xticklabels(x_labels)
    ax.tick_params(width=5, length=15)
    ax.tick_params(which='minor', width=2, length=10)

    for i, line in enumerate(ax.get_lines()):
        if i % 3 == 0:
            line.set_marker(MARKERS[int(i / 3)])
            line.set_markeredgewidth(2)
            line.set_fillstyle('none')
            line.set_markersize(20)

    if right_axis_column is not None:
        right_values = data_frame.groupby(['Benchmark', 'Size'])[right_axis_column]
        right_medians = right_values.median().unstack().transpose()
        right_deviations = right_values.std().unstack().transpose()

        right_medians.axes[0].name = 'Size in Byte'
        right_deviations.axes[0].name = 'Size in Byte'

        ax = right_medians.plot(ax=ax, linestyle=right_axis_linestyle, elinewidth=2, capthick=2,
                                capsize=5, legend=False, grid=True, logx=True, logy=right_logy, secondary_y=True,
                                xticks=x_ticks, xlim=(x_ticks[0], x_ticks[len(x_ticks) - 1]), color=colors,
                                yerr=(right_deviations if right_yerr else False))

        ax.set_ylabel(right_axis_title)

        ax.xaxis.set_tick_params(which='minor', bottom=False)
        ax.set_xticklabels(x_labels)
        ax.tick_params(width=5, length=15)
        ax.tick_params(which='minor', width=2, length=10)

        for i, line in enumerate(ax.get_lines()):
            if i % 3 == 0:
                line.set_marker(MARKERS[int(i / 3)])
                line.set_markeredgewidth(2)
                line.set_fillstyle('none')
                line.set_markersize(20)

    handles, labels = ax.get_legend_handles_labels()
    handles = [h[0] for h in handles]
    ax.legend(handles, labels, loc='upper center', ncol=LEGEND_COLUMNS, bbox_to_anchor=LEGEND_POSITION)

    matplotlib.pyplot.savefig(output_file, bbox_inches='tight')


parser = argparse.ArgumentParser(prog='observatory', formatter_class=argparse.ArgumentDefaultsHelpFormatter)
parser.add_argument('-i', '--input', dest='input_file', required=True, help='Path to the input CSV file')
parser.add_argument('-o', '--output', dest='output_dir', default="./plot/", help='Output path for the plot files')
parser.add_argument('-f', '--format', dest='output_format', default="svg",
                    help='Output format for the plot files (e.g. png, svg, pdf, ...)')

args = parser.parse_args()

try:
    makedirs(args.output_dir)
except FileExistsError:
    pass

file = args.input_file
data_frame = pandas.read_csv(file)

if 'Throughput' in file:
    data_frame['DataThroughput'] = data_frame['DataThroughput'].apply(lambda x: x / 1000000000)
    data_frame['OperationThroughput'] = data_frame['OperationThroughput'].apply(lambda x: x / 1000000)

    plot(data_frame, '{}/Throughput.{}'.format(args.output_dir, args.output_format), 'OperationThroughput',
         'Throughput in MOperations/s', 'dashed', False, True, 'DataThroughput', 'Throughput in GByte/s', 'solid')
if 'Latency' in file or 'PingPong' in file:
    data_frame['OperationThroughput'] = data_frame['OperationThroughput'].apply(lambda x: x / 1000000)
    data_frame['AverageLatency'] = data_frame['AverageLatency'].apply(lambda x: x * 1000000)
    data_frame['MinimumLatency'] = data_frame['MinimumLatency'].apply(lambda x: x * 1000000)
    data_frame['MaximumLatency'] = data_frame['MaximumLatency'].apply(lambda x: x * 1000000)
    data_frame['50thLatency'] = data_frame['50thLatency'].apply(lambda x: x * 1000000)
    data_frame['95thLatency'] = data_frame['95thLatency'].apply(lambda x: x * 1000000)
    data_frame['99thLatency'] = data_frame['99thLatency'].apply(lambda x: x * 1000000)
    data_frame['999thLatency'] = data_frame['999thLatency'].apply(lambda x: x * 1000000)
    data_frame['9999thLatency'] = data_frame['9999thLatency'].apply(lambda x: x * 1000000)

    plot(data_frame, '{}/AverageLatency.{}'.format(args.output_dir, args.output_format), 'AverageLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/MinimumLatency.{}'.format(args.output_dir, args.output_format), 'MinimumLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/MaximumLatency.{}'.format(args.output_dir, args.output_format), 'MaximumLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/50thLatency.{}'.format(args.output_dir, args.output_format), '50thLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/95thLatency.{}'.format(args.output_dir, args.output_format), '95thLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/99thLatency.{}'.format(args.output_dir, args.output_format), '99thLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/999thLatency.{}'.format(args.output_dir, args.output_format), '999thLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
    plot(data_frame, '{}/9999thLatency.{}'.format(args.output_dir, args.output_format), '9999thLatency',
         'Latency in μs', 'solid', True, True, 'OperationThroughput', 'Throughput in MOperations/s', 'dashed')
if 'DataOverhead' in data_frame.columns:
    plot(file[:-4] + ' Data Overhead', data_frame,
         '{}/Data Overhead.{}'.format(args.output_dir, args.output_format), 'DataOverhead',
         'Data Overhead in Byte')
    plot(file[:-4] + ' Data Overhead Factor', data_frame,
         '{}/Data Overhead Factor.{}'.format(args.output_dir, args.output_format), 'DataOverheadFactor',
         'Data Overhead Factor', 'solid', True)
    plot(file[:-4] + ' Overhead', data_frame,
         '{}/Throughput Overhead.{}'.format(args.output_dir, args.output_format), 'DataThroughputOverhead',
         'Data Overhead Throughput in Byte/s')
    '''plot(data_frame, '{}/Data Overhead Percentage.{}'.format(args.output_dir, args.output_format),
         'DataOverheadPercentage', 'Data Overhead Percentage', 'solid', True, False)'''
