import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { ElectionService } from '../../../core/services/election.service';
import { ElectionResults } from '../../../models/election.model';

// PrimeNG
import { CardModule } from 'primeng/card';
import { ButtonModule } from 'primeng/button';
import { ChartModule } from 'primeng/chart';
import { TableModule } from 'primeng/table';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-election-results',
  standalone: true,
  imports: [
    CommonModule,
    CardModule,
    ButtonModule,
    ChartModule,
    TableModule,
    ToastModule
  ],
  providers: [MessageService],
  templateUrl: './election-results.component.html',
  styleUrl: './election-results.component.scss'
})
export class ElectionResultsComponent implements OnInit {
  electionId: string | null = null;
  results: ElectionResults | null = null;
  loading: boolean = true;

  // Chart data
  chartData: any;
  chartOptions: any;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private electionService: ElectionService,
    private messageService: MessageService
  ) {}

  ngOnInit() {
    this.electionId = this.route.snapshot.paramMap.get('id');
    if (this.electionId) {
      this.loadResults(this.electionId);
    } else {
      this.router.navigate(['/admin']);
    }

    this.initChartOptions();
  }

  loadResults(id: string) {
    this.loading = true;
    this.electionService.getElectionResults(id).subscribe({
      next: (data) => {
        this.results = data;
        this.initChartData();
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading results', err);
        this.messageService.add({severity:'error', summary:'Error', detail:'No se pudieron cargar los resultados'});
        this.loading = false;
      }
    });
  }

  initChartData() {
    if (!this.results) return;

    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');

    this.chartData = {
      labels: this.results.results.map(r => r.label),
      datasets: [
        {
          data: this.results.results.map(r => r.votes),
          backgroundColor: [
            documentStyle.getPropertyValue('--blue-500'),
            documentStyle.getPropertyValue('--yellow-500'),
            documentStyle.getPropertyValue('--green-500'),
            documentStyle.getPropertyValue('--pink-500'),
            documentStyle.getPropertyValue('--cyan-500')
          ],
          hoverBackgroundColor: [
            documentStyle.getPropertyValue('--blue-400'),
            documentStyle.getPropertyValue('--yellow-400'),
            documentStyle.getPropertyValue('--green-400'),
            documentStyle.getPropertyValue('--pink-400'),
            documentStyle.getPropertyValue('--cyan-400')
          ]
        }
      ]
    };
  }

  initChartOptions() {
    const documentStyle = getComputedStyle(document.documentElement);
    const textColor = documentStyle.getPropertyValue('--text-color');

    this.chartOptions = {
      plugins: {
        legend: {
          labels: {
            usePointStyle: true,
            color: textColor
          }
        }
      }
    };
  }

  goBack() {
    this.router.navigate(['/admin']);
  }
}
